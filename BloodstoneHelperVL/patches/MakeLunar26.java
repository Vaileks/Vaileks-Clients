import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;import static org.objectweb.asm.Opcodes.*;
public final class MakeLunar26 {
 static final String P="ru/bloodhelper/client/";
 static final Map<String,Set<String>> OPTIONAL=Map.of(
 "FreeLookMouseMixin",Set.of("bloodhelper$freeLook"),"FreeLookCameraMixin",Set.of("bloodhelper$camera"),
 "CrosshairMixin",Set.of("bloodhelper$crosshair","bloodhelper$thirdPerson"),"BetterNameDistanceMixin",Set.of("bloodhelper$nameDistance"),"BetterNameStyleMixin",Set.of("bloodhelper$nameStyle"));
 static byte[] patch(byte[] b){ClassNode c=new ClassNode();new ClassReader(b).accept(c,0);boolean changed=false;
  if(c.name.equals(P+"compat/ClientDistribution"))for(MethodNode m:c.methods)if(m.name.equals("lunarClient")){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new InsnNode(ICONST_1));m.instructions.add(new InsnNode(IRETURN));changed=true;}
  String shortName=c.name.substring(c.name.lastIndexOf('/')+1);Set<String> optional=OPTIONAL.get(shortName);
  if(optional!=null)for(MethodNode m:c.methods)if(optional.contains(m.name)){
   List<AnnotationNode> annotations=new ArrayList<>();if(m.visibleAnnotations!=null)annotations.addAll(m.visibleAnnotations);if(m.invisibleAnnotations!=null)annotations.addAll(m.invisibleAnnotations);
   for(AnnotationNode a:annotations)if(a.desc.startsWith("Lorg/spongepowered/asm/mixin/injection/")||a.desc.startsWith("Lcom/llamalad7/mixinextras/injector/")){
    if(a.values==null)a.values=new ArrayList<>();int index=a.values.indexOf("require");if(index>=0)a.values.set(index+1,0);else{a.values.add("require");a.values.add(0);}changed=true;
   }
  }
  if(!changed)return b;ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);return w.toByteArray();
 }
 public static void main(String[] a)throws Exception {
  Path src=Path.of(a[0]),dst=Path.of(a[1]),helpers=Path.of(a[2]);Files.createDirectories(dst.getParent());
  try(var z=new ZipFile(src.toFile());var out=new ZipOutputStream(Files.newOutputStream(dst))){for(var es=z.entries();es.hasMoreElements();){var e=es.nextElement();String n=e.getName();byte[] b=z.getInputStream(e).readAllBytes();if(n.endsWith(".class")){Path helper=helpers.resolve(n);b=Files.exists(helper)?Files.readAllBytes(helper):patch(b);}out.putNextEntry(new ZipEntry(n));out.write(b);out.closeEntry();}}
 }
}
