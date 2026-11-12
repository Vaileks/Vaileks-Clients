import java.lang.classfile.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;
public class VerifyAll {
 public static void main(String[] args)throws Exception{
  var urls=new ArrayList<URL>();for(String p:Files.readAllLines(Path.of(args[0])))if(!p.isBlank())urls.add(Path.of(p).toUri().toURL());
  for(int i=1;i<args.length;i++)urls.add(Path.of(args[i]).toUri().toURL());
  try(var loader=new URLClassLoader(urls.toArray(URL[]::new))){var cf=ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(ClassHierarchyResolver.ofResourceParsing(loader).cached()));
   for(int i=1;i<args.length;i++){String path=args[i];int count=0;try(var z=new ZipFile(path)){var entries=z.entries();while(entries.hasMoreElements()){var e=entries.nextElement();if(!e.getName().endsWith(".class"))continue;var errors=cf.verify(z.getInputStream(e).readAllBytes());if(!errors.isEmpty())throw new AssertionError(e.getName()+errors);count++;}}System.out.println("PASS class verification: "+path+" / "+count+" classes");}
  }
 }
}
