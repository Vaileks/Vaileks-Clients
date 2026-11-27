import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.objectweb.asm.Opcodes.*;

/** Remove the resizing module registry entry and its executable classes. */
public final class RemoveLegendarySize {
 static final String P="ru/bloodhelper/client/",MODULE=P+"gui/BloodHelperScreen$Module",SIZE=P+"modules/LegendarySizeModule",SCREEN=P+"modules/HandItemScaleScreen";
 static boolean removed(String n){return n.equals(SIZE)||n.startsWith(SIZE+"$")||n.equals(SCREEN)||n.startsWith(SCREEN+"$");}
 static AbstractInsnNode previous(AbstractInsnNode n){do{n=n.getPrevious();}while(n!=null&&n.getOpcode()<0);return n;}
 static void push(InsnList list,int n){list.add(new LdcInsnNode(n));}
 static void result(InsnList out,Type t,boolean scale){
  switch(t.getSort()){
   case Type.VOID:break;
   case Type.FLOAT:out.add(new InsnNode(scale?FCONST_1:FCONST_0));break;
   case Type.DOUBLE:out.add(new InsnNode(scale?DCONST_1:DCONST_0));break;
   case Type.LONG:out.add(new InsnNode(LCONST_0));break;
   case Type.OBJECT:case Type.ARRAY:out.add(new InsnNode(ACONST_NULL));break;
   default:out.add(new InsnNode(ICONST_0));
  }
 }
 static void empty(MethodNode m){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.visibleLocalVariableAnnotations=null;m.invisibleLocalVariableAnnotations=null;Type ret=Type.getReturnType(m.desc);result(m.instructions,ret,false);m.instructions.add(new InsnNode(ret.getOpcode(IRETURN)));}
 static byte[] patch(byte[] input){
  ClassNode c=new ClassNode();new ClassReader(input).accept(c,0);boolean changed=false;
  if(c.name.equals(MODULE)){
   var ids=new LinkedHashMap<String,String>();
   MethodNode init=c.methods.stream().filter(m->m.name.equals("<clinit>")).findFirst().orElseThrow();
   for(AbstractInsnNode n:init.instructions.toArray())if(n instanceof FieldInsnNode f&&f.getOpcode()==PUTSTATIC&&f.owner.equals(MODULE)&&!f.name.equals("$VALUES")){
    AbstractInsnNode call=previous(n),id=previous(call);
    if(!(id instanceof LdcInsnNode literal)||!(literal.cst instanceof String))throw new IllegalStateException("Unexpected enum initializer "+f.name);
    if(!f.name.equals("LEGENDARY_SIZE"))ids.put(f.name,(String)literal.cst);
   }
   if(ids.size()!=66)throw new IllegalStateException("Expected 66 retained modules: "+ids.size());
   c.fields.removeIf(f->f.name.equals("LEGENDARY_SIZE"));
   init.instructions.clear();init.tryCatchBlocks.clear();if(init.localVariables!=null)init.localVariables.clear();
   int ordinal=0;
   for(var entry:ids.entrySet()){
    var out=init.instructions;out.add(new TypeInsnNode(NEW,MODULE));out.add(new InsnNode(DUP));out.add(new LdcInsnNode(entry.getKey()));push(out,ordinal++);out.add(new LdcInsnNode(entry.getValue()));out.add(new MethodInsnNode(INVOKESPECIAL,MODULE,"<init>","(Ljava/lang/String;ILjava/lang/String;)V",false));out.add(new FieldInsnNode(PUTSTATIC,MODULE,entry.getKey(),"L"+MODULE+";"));
   }
   init.instructions.add(new MethodInsnNode(INVOKESTATIC,MODULE,"$values","()[L"+MODULE+";",false));init.instructions.add(new FieldInsnNode(PUTSTATIC,MODULE,"$VALUES","[L"+MODULE+";"));init.instructions.add(new InsnNode(RETURN));
   MethodNode values=c.methods.stream().filter(m->m.name.equals("$values")).findFirst().orElseThrow();values.instructions.clear();values.tryCatchBlocks.clear();if(values.localVariables!=null)values.localVariables.clear();
   push(values.instructions,ids.size());values.instructions.add(new TypeInsnNode(ANEWARRAY,MODULE));int i=0;for(String field:ids.keySet()){values.instructions.add(new InsnNode(DUP));push(values.instructions,i++);values.instructions.add(new FieldInsnNode(GETSTATIC,MODULE,field,"L"+MODULE+";"));values.instructions.add(new InsnNode(AASTORE));}values.instructions.add(new InsnNode(ARETURN));changed=true;
  }
  for(MethodNode m:c.methods){
   boolean createsRemovedScreen=false;
   for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof TypeInsnNode t&&t.getOpcode()==NEW&&removed(t.desc))createsRemovedScreen=true;
   if(createsRemovedScreen){if(!m.name.startsWith("lambda$"))throw new IllegalStateException("Unexpected screen creator "+c.name+"."+m.name);empty(m);changed=true;continue;}
   for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode call&&removed(call.owner)){
    if(call.getOpcode()!=INVOKESTATIC)throw new IllegalStateException("Unexpected removed instance call "+call.owner+"."+call.name);
    InsnList replacement=new InsnList();Type[] args=Type.getArgumentTypes(call.desc);for(int i=args.length-1;i>=0;i--)replacement.add(new InsnNode(args[i].getSize()==2?POP2:POP));result(replacement,Type.getReturnType(call.desc),call.name.equals("scale"));m.instructions.insertBefore(n,replacement);m.instructions.remove(n);changed=true;
   }
  }
  if(!changed)return input;
  ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);return out.toByteArray();
 }
 public static void main(String[] args)throws Exception{
  Path input=Path.of(args[0]),output=Path.of(args[1]);int removedCount=0;
  try(var zin=new ZipFile(input.toFile());var zout=new ZipOutputStream(Files.newOutputStream(output))){
   for(var entries=zin.entries();entries.hasMoreElements();){ZipEntry entry=entries.nextElement();String n=entry.getName();
    if(n.endsWith(".class")&&removed(n.substring(0,n.length()-6))){removedCount++;continue;}
    byte[] data=zin.getInputStream(entry).readAllBytes();if(n.startsWith(P)&&n.endsWith(".class"))data=patch(data);
    ZipEntry next=new ZipEntry(n);next.setTime(entry.getTime());zout.putNextEntry(next);zout.write(data);zout.closeEntry();
   }
  }
  if(removedCount<2)throw new IllegalStateException("No resizing implementation removed");
  System.out.println("Removed resizing module registry entry and "+removedCount+" implementation classes: "+output);
 }
}
