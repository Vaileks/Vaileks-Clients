package pyz1k.extensions;
import java.util.*;
public final class ProgressLogicTest {
 static int checks;static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 public static void main(String[] args){
  var c=new ProgressLogic.Clock();check(c.advance("a",0)==0,"first join");check(c.advance("a",1_000_000_000L)==1000,"time accrues");check(c.advance("b",2_000_000_000L)==0,"server switch does not attribute a to b");check(c.advance("b",3_000_000_000L)==1000,"second server accrues");check(c.advance("",4_000_000_000L)==0,"disconnect");check(c.advance("",9_000_000_000L)==0,"menu not counted");check(c.advance("a",10_000_000_000L)==0,"reconnect starts fresh");check(c.advance("a",30_000_000_000L)==0,"sleep not counted");check(c.advance("a",31_000_000_000L)==1000,"resume");
  var fractional=new ProgressLogic.Clock();fractional.advance("a",0);long total=0;for(int i=1;i<=2000;i++)total+=fractional.advance("a",i*500_000L);check(total==1000,"sub-millisecond time preserved");
  check(ProgressLogic.normalizeServer(" EXAMPLE.ORG:25565 ").equals("example.org"),"default port normalized");check(!ProgressLogic.normalizeServer("example.org:25566").equals("example.org"),"custom port isolated");check(ProgressLogic.normalizeServer("[::1]:25565").equals("[::1]"),"IPv6 default port");check(ProgressLogic.duration(3_661_000).equals("1 ч 1 мин 1 с"),"time format");
  var list=new ProgressLogic.Checklist(List.of(List.of("plains"),List.of("bread"),List.of("cow","pig")),Set.of("plains","cow"),true);check(list.fulfilled()==2,"AND groups with OR alternatives");check(list.state("plains")==ProgressLogic.State.DONE,"completed criterion");check(list.state("bread")==ProgressLogic.State.REMAINING,"remaining criterion");check(list.state("pig")==ProgressLogic.State.ALTERNATIVE,"unneeded OR alternative");
  var overlap=new ProgressLogic.Checklist(List.of(List.of("a","b"),List.of("b")),Set.of("a"),true);check(overlap.state("b")==ProgressLogic.State.REMAINING,"criterion still needed in another AND group");
  var unknown=new ProgressLogic.Checklist(list.groups(),Set.of(),false);check(unknown.state("bread")==ProgressLogic.State.UNKNOWN,"missing server progress is unknown");check(unknown.fulfilled()==0,"unknown not complete");
  check(new ProgressLogic.Checklist(List.of(),Set.of(),true).names().isEmpty(),"empty requirement safe");
  System.out.println("PASS "+checks+" clock, server identity and advancement checklist checks");
 }
}
