package ru.bloodhelper.client.compat;
import org.objectweb.asm.tree.*;import static org.objectweb.asm.Opcodes.*;
public final class TestLunarHooks {
 static ClassNode target(String... names){ClassNode n=new ClassNode();n.name="test/Target";MethodNode m=new MethodNode();for(String name:names)m.instructions.add(new MethodInsnNode(INVOKESTATIC,n.name,"prefix$"+name,"()V",false));n.methods.add(m);return n;}
 static void check(boolean b){if(!b)throw new AssertionError("Lunar compatibility hook state");}
 public static void main(String[] a){LunarMixinPlugin plugin=new LunarMixinPlugin();
  plugin.postApply("",target(),"FreeLookMouseMixin",null);plugin.postApply("",target(),"FreeLookCameraMixin",null);check(!OptionalMixinHooks.freeLook());
  plugin.postApply("",target("bloodhelper$freeLook"),"FreeLookMouseMixin",null);plugin.postApply("",target("bloodhelper$camera"),"FreeLookCameraMixin",null);check(OptionalMixinHooks.freeLook());
  plugin.postApply("",target(),"CrosshairMixin",null);check(!OptionalMixinHooks.crosshair());check(!OptionalMixinHooks.crosshairPerspective());
  plugin.postApply("",target("bloodhelper$crosshair","bloodhelper$thirdPerson"),"CrosshairMixin",null);check(OptionalMixinHooks.crosshair());check(OptionalMixinHooks.crosshairPerspective());
  plugin.postApply("",target(),"BetterNameDistanceMixin",null);check(!OptionalMixinHooks.nameDistance());plugin.postApply("",target("bloodhelper$nameDistance"),"BetterNameDistanceMixin",null);check(OptionalMixinHooks.nameDistance());
  System.out.println("PASS: missing Lunar hook call sites disable affected features; restored hooks re-enable camera, crosshair and name-distance integration");
 }
}
