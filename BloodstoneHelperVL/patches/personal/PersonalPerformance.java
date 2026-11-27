package ru.bloodhelper.client.vl;
public final class PersonalPerformance {
 private PersonalPerformance(){}
 private static Object field(Object o,String name)throws ReflectiveOperationException{return o.getClass().getField(name).get(o);}
 private static void set(Object o,String name,Object value)throws ReflectiveOperationException{o.getClass().getField(name).set(o,value);}
 public static void apply(Object cfg){try{
  set(cfg,"hudMenuEffects",false);Object quality=field(cfg,"renderQuality");if(quality!=null){set(quality,"glassQuality","LOW");set(quality,"shadows",false);}
  Object optimizer=field(cfg,"optimizer");if(optimizer!=null){set(optimizer,"enabled",true);set(optimizer,"hideGrass",false);set(optimizer,"disableEntityShadows",true);set(optimizer,"decorativeParticles",true);set(optimizer,"signs",true);set(optimizer,"signDistance",24);set(optimizer,"signTextDistance",12);Object filter=field(optimizer,"droppedItems");if(filter!=null)set(filter,"enabled",false);}
 }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
}
