import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.objectweb.asm.Opcodes.*;
public class PersonalPatcher {
 static final String P="ru/bloodhelper/client/",R=P+"visuals/MotionBlurRenderer",B=R+"$Buffers";
 static final String TEX="Lcom/mojang/blaze3d/textures/GpuTexture;",VIEW="Lcom/mojang/blaze3d/textures/GpuTextureView;";
 static AbstractInsnNode prev(AbstractInsnNode n){do{n=n.getPrevious();}while(n!=null&&n.getOpcode()<0);return n;}
 static FieldInsnNode buffer(){return new FieldInsnNode(GETSTATIC,R,"buffers","L"+B+";");}
 static InsnList field(String name,String type){var l=new InsnList();l.add(buffer());l.add(new FieldInsnNode(GETFIELD,B,name,type));return l;}
 static void require(boolean b,String m){if(!b)throw new IllegalStateException(m);}
 static byte[] optimizeBlur(byte[] data){
  var c=new ClassNode();new ClassReader(data).accept(c,0);
  if(c.name.equals(B)){
   var ctor=c.methods.stream().filter(m->m.name.equals("<init>")).findFirst().orElseThrow();int usage=0;
   for(var n:ctor.instructions.toArray())if(n.getOpcode()==ICONST_5){ctor.instructions.set(n,new IntInsnNode(BIPUSH,15));usage++;}require(usage==1,"Texture usage pattern");
   var swap=new MethodNode(ACC_SYNTHETIC,"vl$swap","()V",null,null);
   swap.instructions.add(new VarInsnNode(ALOAD,0));swap.instructions.add(new FieldInsnNode(GETFIELD,B,"current",TEX));swap.instructions.add(new VarInsnNode(ASTORE,1));
   swap.instructions.add(new VarInsnNode(ALOAD,0));swap.instructions.add(new FieldInsnNode(GETFIELD,B,"currentView",VIEW));swap.instructions.add(new VarInsnNode(ASTORE,2));
   for(var pair:List.of(new String[]{"current","history",TEX},new String[]{"currentView","historyView",VIEW})){
    swap.instructions.add(new VarInsnNode(ALOAD,0));swap.instructions.add(new VarInsnNode(ALOAD,0));swap.instructions.add(new FieldInsnNode(GETFIELD,B,pair[1],pair[2]));swap.instructions.add(new FieldInsnNode(PUTFIELD,B,pair[0],pair[2]));
   }
   swap.instructions.add(new VarInsnNode(ALOAD,0));swap.instructions.add(new VarInsnNode(ALOAD,1));swap.instructions.add(new FieldInsnNode(PUTFIELD,B,"history",TEX));
   swap.instructions.add(new VarInsnNode(ALOAD,0));swap.instructions.add(new VarInsnNode(ALOAD,2));swap.instructions.add(new FieldInsnNode(PUTFIELD,B,"historyView",VIEW));swap.instructions.add(new InsnNode(RETURN));c.methods.add(swap);
  }else{
   var m=c.methods.stream().filter(x->x.name.equals("compositeWorld")).findFirst().orElseThrow();var copies=new ArrayList<MethodInsnNode>();
   for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode x&&x.name.equals("copyTextureToTexture"))copies.add(x);
   require(copies.size()==3,"Expected seed + two full-size copies");
   var remove=copies.get(1);AbstractInsnNode start=remove;
   while(!(start instanceof VarInsnNode v&&v.getOpcode()==ALOAD&&v.var==11))start=start.getPrevious();
   var stop=remove.getNext();for(var n=start;n!=stop;){var next=n.getNext();m.instructions.remove(n);n=next;}
   int outputs=0,inputs=0;
   for(var n:m.instructions.toArray()){
    if(n instanceof MethodInsnNode call&&call.owner.equals("net/minecraft/class_276")&&call.name.equals("method_71639")){
     var load=prev(n);require(load instanceof VarInsnNode targetLoad&&targetLoad.var==1,"Render pass target");m.instructions.insertBefore(load,field("currentView",VIEW));m.instructions.remove(load);m.instructions.remove(n);outputs++;
    }else if(n instanceof FieldInsnNode f&&f.owner.equals(B)&&f.name.equals("currentView")){
     // The freshly inserted output view is excluded: iterate over the original node array.
     var old=prev(n);require(old instanceof FieldInsnNode,"CurrentFrame binding");var replacement=new InsnList();replacement.add(new VarInsnNode(ALOAD,1));replacement.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/class_276","method_71639","()"+VIEW,false));m.instructions.insertBefore(old,replacement);m.instructions.remove(old);m.instructions.remove(n);inputs++;
    }
   }require(outputs==1&&inputs==1,"Blur views rewiring");
   var last=copies.get(2);AbstractInsnNode begin=last;while(!(begin instanceof VarInsnNode copyLoad&&copyLoad.getOpcode()==ALOAD&&copyLoad.var==11))begin=begin.getPrevious();
   var source=begin.getNext();while(source.getOpcode()<0)source=source.getNext();require(source instanceof VarInsnNode sourceLoad&&sourceLoad.var==2,"Final source");m.instructions.insertBefore(source,field("current",TEX));m.instructions.remove(source);
   // The destination was buffers.history; output goes back to the game's color attachment.
   FieldInsnNode dest=null;for(var n=begin;n!=last;n=n.getNext())if(n instanceof FieldInsnNode f&&f.owner.equals(B)&&f.name.equals("history"))dest=f;
   require(dest!=null,"Final history destination");var receiver=prev(dest);m.instructions.insertBefore(receiver,new VarInsnNode(ALOAD,2));m.instructions.remove(receiver);m.instructions.remove(dest);
   var swap=new InsnList();swap.add(buffer());swap.add(new MethodInsnNode(INVOKEVIRTUAL,B,"vl$swap","()V",false));m.instructions.insert(last,swap);
  }
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);return w.toByteArray();
 }
 static byte[] profile(byte[] bytes){var c=new ClassNode();new ClassReader(bytes).accept(c,0);for(var m:c.methods)if((c.name.equals(P+"config/ClientConfig")&&(m.name.equals("sanitize")||m.name.equals("<init>")))||(c.name.equals(P+"policy/RuntimeConfig")&&m.name.equals("refresh")))for(var n:m.instructions.toArray())if(n.getOpcode()==RETURN){var ins=new InsnList();ins.add(new VarInsnNode(ALOAD,0));ins.add(new MethodInsnNode(INVOKESTATIC,P+"vl/PersonalPerformance","apply","(Ljava/lang/Object;)V",false));m.instructions.insertBefore(n,ins);}var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);return w.toByteArray();}
 public static void main(String[] args)throws Exception{
  Path input=Path.of(args[0]),out=Path.of(args[1]);Files.createDirectories(out.getParent());
  try(var src=new ZipFile(input.toFile());var dst=new ZipOutputStream(Files.newOutputStream(out))){var entries=src.entries();while(entries.hasMoreElements()){var e=entries.nextElement();byte[] b=src.getInputStream(e).readAllBytes();if(e.getName().equals(R+".class")||e.getName().equals(B+".class"))b=optimizeBlur(b);if(e.getName().equals(P+"config/ClientConfig.class")||e.getName().equals(P+"policy/RuntimeConfig.class"))b=profile(b);if(e.getName().equals(P+"config/RenderQualitySettings.class"))b=Files.readAllBytes(Path.of(args[2],"ru/bloodhelper/client/config/RenderQualitySettings.class"));var ne=new ZipEntry(e.getName());ne.setTime(e.getTime());dst.putNextEntry(ne);dst.write(b);dst.closeEntry();}dst.putNextEntry(new ZipEntry(P+"vl/PersonalPerformance.class"));dst.write(Files.readAllBytes(Path.of(args[2],"ru/bloodhelper/client/vl/PersonalPerformance.class")));dst.closeEntry();}
  System.out.println("PASS: Motion Blur ping-pong, one copy per blended frame");
 }
}
