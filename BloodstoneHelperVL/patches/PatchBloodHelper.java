import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;
public class PatchBloodHelper {
 static final String P="ru/bloodhelper/client/",H=P+"vl/VLRestrictions";
 static final Set<String> STUB=Set.of("visuals/LootHighlight","modules/LegendarySizeModule");
 static int changedMethods;
 static void neutral(MethodVisitor m,String name,String desc,String owner){
  Type t=Type.getReturnType(desc);boolean scale=(owner.endsWith("LegendarySizeModule")||owner.endsWith("LegendarySizeSettings"))&&(name.equals("scale")||name.equals("handScale")||name.equals("groundScale"));
  switch(t.getSort()){
   case Type.VOID:m.visitInsn(RETURN);break;
   case Type.BOOLEAN:case Type.BYTE:case Type.SHORT:case Type.CHAR:case Type.INT:m.visitInsn(ICONST_0);m.visitInsn(IRETURN);break;
   case Type.FLOAT:m.visitInsn(scale?FCONST_1:FCONST_0);m.visitInsn(FRETURN);break;
   case Type.DOUBLE:m.visitInsn(scale?DCONST_1:DCONST_0);m.visitInsn(DRETURN);break;
   case Type.LONG:m.visitInsn(LCONST_0);m.visitInsn(LRETURN);break;
   default:
    if(t.getInternalName().equals("java/util/List"))m.visitMethodInsn(INVOKESTATIC,"java/util/List","of","()Ljava/util/List;",true);
    else m.visitInsn(ACONST_NULL);
    m.visitInsn(ARETURN);
  }
  m.visitMaxs(2,0);
 }
 static byte[] patch(byte[] bytes){
  int before=changedMethods;ClassReader r=new ClassReader(bytes);String owner=r.getClassName();boolean stub=STUB.contains(owner.substring(P.length()));
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);
  r.accept(new ClassVisitor(ASM9,w){
   public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
    MethodVisitor base=super.visitMethod(a,n,d,s,e);
    boolean replace=stub&&(a&ACC_STATIC)!=0&&!n.equals("<clinit>");
    if(owner.equals(P+"access/DeviceIdentity")&&Set.of("loadOrCreate","readOrCreate").contains(n))replace=true;
    if(owner.equals(P+"config/LegendarySizeSettings")&&Set.of("scale","handScale","groundScale","setHandScale").contains(n))replace=true;
    if(replace){changedMethods++;return new MethodVisitor(ASM9,base){public void visitCode(){super.visitCode();neutral(mv,n,d,owner);} public void visitMaxs(int x,int y){} public void visitEnd(){super.visitEnd();}
     public void visitInsn(int x){} public void visitIntInsn(int x,int y){} public void visitVarInsn(int x,int y){} public void visitTypeInsn(int x,String y){} public void visitFieldInsn(int x,String y,String z,String q){} public void visitMethodInsn(int x,String y,String z,String q,boolean it){} public void visitInvokeDynamicInsn(String x,String y,Handle z,Object...q){} public void visitJumpInsn(int x,Label y){} public void visitLabel(Label x){} public void visitLdcInsn(Object x){} public void visitIincInsn(int x,int y){} public void visitTableSwitchInsn(int x,int y,Label z,Label...q){} public void visitLookupSwitchInsn(Label x,int[] y,Label[] z){} public void visitMultiANewArrayInsn(String x,int y){} public void visitTryCatchBlock(Label x,Label y,Label z,String q){} public void visitLocalVariable(String x,String y,String z,Label q,Label v,int u){} public void visitLineNumber(int x,Label y){} public void visitFrame(int x,int y,Object[] z,int q,Object[] v){}
    };}
    return new MethodVisitor(ASM9,base){
     public void visitCode(){
      super.visitCode();if(owner.equals(P+"gui/BloodHelperScreen")&&n.equals("button")){
       Label keep=new Label();super.visitVarInsn(ALOAD,2);super.visitMethodInsn(INVOKESTATIC,H,"hideButton","(Ljava/lang/String;)Z",false);super.visitJumpInsn(IFEQ,keep);super.visitInsn(RETURN);super.visitLabel(keep);super.visitFrame(F_SAME,0,null,0,null);changedMethods++;
      }
     }
     public void visitInvokeDynamicInsn(String name,String desc,Handle bootstrap,Object...args){
      if(owner.equals(P+"gui/BloodHelperScreen")){for(int i=0;i<args.length;i++)if(args[i] instanceof String text&&text.contains("BLOODHELPER 1.3.8")){args[i]=text.replace("BLOODHELPER 1.3.8","BloodstoneHelperVL 1.3.8 · Vaileks");changedMethods++;}}
      super.visitInvokeDynamicInsn(name,desc,bootstrap,args);
     }
     public void visitMethodInsn(int op,String cl,String name,String desc,boolean isInterface){
      if(!owner.startsWith(P+"discord/")&&!owner.startsWith(P+"vl/")){
       if((cl.equals("java/net/URI")&&name.equals("toURL"))||(cl.equals("java/net/http/HttpRequest")&&name.equals("newBuilder")&&desc.startsWith("(Ljava/net/URI;"))||(cl.equals("java/net/http/HttpRequest$Builder")&&name.equals("uri"))){super.visitInsn(DUP);super.visitMethodInsn(INVOKESTATIC,P+"vl/PrivacyGuard","checkUri","(Ljava/net/URI;)V",false);changedMethods++;}
       if(cl.equals("java/net/URL")&&(name.equals("openConnection")||name.equals("openStream"))&&desc.startsWith("()")){super.visitInsn(DUP);super.visitMethodInsn(INVOKESTATIC,P+"vl/PrivacyGuard","checkUrlObject","(Ljava/net/URL;)V",false);changedMethods++;}
      }
      super.visitMethodInsn(op,cl,name,desc,isInterface);
      if(owner.equals(P+"gui/BloodHelperScreen")&&cl.equals(P+"gui/BloodHelperScreen$Module")&&name.equals("values")){
       super.visitMethodInsn(INVOKESTATIC,H,"visibleModules","([Ljava/lang/Object;)[Ljava/lang/Object;",false);super.visitTypeInsn(CHECKCAST,"[L"+cl+";");changedMethods++;
      }
     }
     public void visitInsn(int op){
      if(op==RETURN&&owner.equals(P+"config/ClientConfig")&&(n.equals("sanitize")||n.equals("<init>"))){super.visitVarInsn(ALOAD,0);super.visitMethodInsn(INVOKESTATIC,H,"enforce","(Ljava/lang/Object;)V",false);changedMethods++;}
      if(op==RETURN&&owner.equals(P+"policy/RuntimeConfig")&&n.equals("refresh")){super.visitVarInsn(ALOAD,0);super.visitMethodInsn(INVOKESTATIC,H,"enforce","(Ljava/lang/Object;)V",false);changedMethods++;}
      if(op==ARETURN&&owner.equals(P+"gui/BloodHelperScreen")&&n.equals("searchable")){
       super.visitVarInsn(ALOAD,2);super.visitMethodInsn(INVOKESTATIC,H,"markRow","(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;",false);super.visitTypeInsn(CHECKCAST,P+"gui/BloodHelperScreen$Row");changedMethods++;
      }
      if(op==ARETURN&&owner.equals(P+"gui/BloodHelperScreen")&&Set.of("rows","moduleRows","findSettings").contains(n)){
       super.visitMethodInsn(INVOKESTATIC,H,"cleanRows","(Ljava/util/List;)Ljava/util/List;",false);changedMethods++;
      }
      super.visitInsn(op);
     }
     public void visitLdcInsn(Object c){
      if(Set.of("gui/BloodHelperScreen","config/WatermarkSettings","hud/HudWidgets","hud/NotificationInbox","hud/Notifications","modules/AppearanceShareScreen","modules/BackgroundAlerts","modules/BindManagerScreen","notifications/WindowsNotificationBackend").contains(owner.substring(P.length()))&&c instanceof String t&&Set.of("BLOODHELPER","BloodHelper","BLOODHELPER (Lunar Client)").contains(t)){c=t.contains("Lunar")?"BloodstoneHelperVL (Lunar Client)":"BloodstoneHelperVL";changedMethods++;}
      if(owner.equals(P+"gui/BloodHelperScreen")&&c instanceof String t){
       if(t.startsWith("https://")||t.startsWith("http://")){c=t.contains("discord.gg/")?"https://discord.gg/9kE6HNJEnZ":"";changedMethods++;}
       else if(t.equals("Discord BloodHelper")){c="Discord BloodstoneHelperVL";changedMethods++;}
      }
      super.visitLdcInsn(c);
     }
    };
   }
  },0);
  return changedMethods==before?bytes:w.toByteArray();
 }
 public static void main(String[] args)throws Exception{
  Path input=Path.of(args[0]),output=Path.of(args[1]);Files.createDirectories(output.getParent());int classes=0;
  try(ZipFile src=new ZipFile(input.toFile());ZipOutputStream dst=new ZipOutputStream(Files.newOutputStream(output))){
   var es=src.entries();while(es.hasMoreElements()){
    ZipEntry e=es.nextElement();byte[] b=src.getInputStream(e).readAllBytes();
    if(e.getName().startsWith(P)&&e.getName().endsWith(".class")){b=patch(b);classes++;}
    ZipEntry ne=new ZipEntry(e.getName());ne.setTime(e.getTime());dst.putNextEntry(ne);dst.write(b);dst.closeEntry();
   }
   dst.putNextEntry(new ZipEntry(H+".class"));dst.write(Files.readAllBytes(Path.of(args[2])));dst.closeEntry();
  }
  System.out.println(output+": "+classes+" classes visited, "+changedMethods+" restrictions applied");
 }
}
