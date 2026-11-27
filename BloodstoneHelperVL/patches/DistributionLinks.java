import org.objectweb.asm.*;
import java.nio.file.*;import java.util.zip.*;
/** Apply distribution-specific platform labels and community links. */
public final class DistributionLinks {
 static final String SCREEN="ru/bloodhelper/client/gui/BloodHelperScreen.class",DISCORD="https://discord.gg/9kE6HNJEnZ";
 static byte[] patch(byte[] data,boolean lunar){ClassReader reader=new ClassReader(data);ClassWriter writer=new ClassWriter(0);reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
  @Override public MethodVisitor visitMethod(int access,String name,String descriptor,String signature,String[] exceptions){return new MethodVisitor(Opcodes.ASM9,super.visitMethod(access,name,descriptor,signature,exceptions)){
   @Override public void visitLdcInsn(Object value){if(value instanceof String text){
    if(name.equals("about")){if(lunar&&text.startsWith("Minecraft "))text=text.replace(" · Fabric"," · Lunar / Fabric");}
    if(text.startsWith("https://discord.gg/")||text.startsWith("http://discord.gg/")||text.startsWith("https://discord.com/invite/"))text=DISCORD;
    value=text;
   }super.visitLdcInsn(value);}
  };}
 },0);return writer.toByteArray();}
 public static void main(String[] args)throws Exception{Path input=Path.of(args[0]),output=Path.of(args[1]);boolean lunar=args.length>2&&Boolean.parseBoolean(args[2]);
 try(var source=new ZipFile(input.toFile());var target=new ZipOutputStream(Files.newOutputStream(output))){for(var e=source.entries();e.hasMoreElements();){var entry=e.nextElement();byte[] bytes=source.getInputStream(entry).readAllBytes();if(entry.getName().equals(SCREEN))bytes=patch(bytes,lunar);ZipEntry next=new ZipEntry(entry.getName());next.setTime(entry.getTime());target.putNextEntry(next);target.write(bytes);target.closeEntry();}}
 }
}
