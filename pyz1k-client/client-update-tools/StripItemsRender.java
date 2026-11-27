import org.objectweb.asm.*;
import java.nio.file.*;
import java.util.zip.*;
import java.io.*;
public final class StripItemsRender {
 static boolean removed(String owner){return owner.startsWith("pyz1k/itemrender/")||owner.startsWith("pyz1k/screen/menu/sections/ItemRenderSection");}
 static void discard(MethodVisitor m,int opcode,String descriptor){
  Type[] args=Type.getArgumentTypes(descriptor);
  for(int i=args.length-1;i>=0;i--)m.visitInsn(args[i].getSize()==2?Opcodes.POP2:Opcodes.POP);
  if(opcode!=Opcodes.INVOKESTATIC)m.visitInsn(Opcodes.POP);
  Type t=Type.getReturnType(descriptor);
  switch(t.getSort()){
   case Type.VOID:break;case Type.LONG:m.visitInsn(Opcodes.LCONST_0);break;case Type.DOUBLE:m.visitInsn(Opcodes.DCONST_0);break;case Type.FLOAT:m.visitInsn(Opcodes.FCONST_0);break;
   case Type.OBJECT:case Type.ARRAY:m.visitInsn(Opcodes.ACONST_NULL);break;default:m.visitInsn(Opcodes.ICONST_0);
  }
 }
 public static void main(String[] args)throws Exception{
  Path path=Path.of(args[0]),tmp=path.resolveSibling(path.getFileName()+".stripped");
  try(ZipFile z=new ZipFile(path.toFile());ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(tmp))){
   var entries=z.entries();while(entries.hasMoreElements()){
    ZipEntry e=entries.nextElement();String n=e.getName();
    if(e.isDirectory()||removed(n)||n.matches("pyz1k/mixin/(EntityItemRenderMixin|ItemEntityRendererMixin|ItemEntityRenderStateMixin)\\.class"))continue;
    byte[] b=z.getInputStream(e).readAllBytes();
    if(n.equals("pyz1k.mixins.json")){
     String s=new String(b,java.nio.charset.StandardCharsets.UTF_8);
     s=s.replaceAll("\\s*\"(?:EntityItemRenderMixin|ItemEntityRendererMixin|ItemEntityRenderStateMixin)\",?","").replaceAll(",\\s*]","]");b=s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    } else if(n.endsWith(".class")){
     ClassReader reader=new ClassReader(b);ClassWriter writer=new ClassWriter(0);String owner=reader.getClassName();
     reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
      @Override public FieldVisitor visitField(int a,String name,String d,String sig,Object val){return removed(Type.getType(d).getSort()==Type.OBJECT?Type.getType(d).getInternalName():"")?null:super.visitField(a,name,d,sig,val);}
      @Override public MethodVisitor visitMethod(int a,String name,String d,String sig,String[] ex){MethodVisitor parent=super.visitMethod(a,name,d,sig,ex);return new MethodVisitor(Opcodes.ASM9,parent){
       boolean hiddenTab=owner.equals("pyz1k/screen/menu/sections/CategoryBarSection")&&name.equals("<clinit>");
       @Override public void visitTypeInsn(int op,String type){if(op==Opcodes.NEW&&removed(type)){super.visitInsn(Opcodes.ACONST_NULL);return;}super.visitTypeInsn(op,type);}
       @Override public void visitFieldInsn(int op,String o,String field,String desc){
        if(hiddenTab&&op==Opcodes.GETSTATIC&&field.equals("ITEM_RENDER"))return;
        if(field.equals("itemRenderSection")&&o.equals("pyz1k/screen/menu/MenuScreen")){
         if(op==Opcodes.PUTFIELD)super.visitInsn(Opcodes.POP2);else if(op==Opcodes.GETFIELD){super.visitInsn(Opcodes.POP);super.visitInsn(Opcodes.ACONST_NULL);}return;
        }super.visitFieldInsn(op,o,field,desc);
       }
       @Override public void visitMethodInsn(int op,String o,String method,String desc,boolean itf){
        if(removed(o)){discard(this.mv,op,desc);return;}
        if(hiddenTab&&o.equals("java/util/List")&&method.equals("of")&&Type.getArgumentTypes(desc).length==6)desc="(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;";
        super.visitMethodInsn(op,o,method,desc,itf);
       }
      };}
     },0);b=writer.toByteArray();
    }
    ZipEntry target=new ZipEntry(n);target.setTime(1794355200000L);out.putNextEntry(target);out.write(b);out.closeEntry();
   }
  }Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING);System.out.println("ItemsRender removed: "+path);
 }
}
