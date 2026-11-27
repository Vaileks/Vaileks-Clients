package pyz1k.extensions;
import java.util.*;
/** Pure accounting and server-supplied checklist semantics. */
public final class ProgressLogic {
 private ProgressLogic(){}
 public enum State { DONE, REMAINING, ALTERNATIVE, UNKNOWN }
 public record Checklist(List<List<String>> groups,Set<String> completed,boolean known){
  public Checklist { groups=groups.stream().map(List::copyOf).toList();completed=Set.copyOf(completed); }
  public Set<String> names(){var n=new LinkedHashSet<String>();groups.forEach(n::addAll);return n;}
  public int fulfilled(){return known?(int)groups.stream().filter(g->g.stream().anyMatch(completed::contains)).count():0;}
  public State state(String id){if(!known)return State.UNKNOWN;if(completed.contains(id))return State.DONE;return groups.stream().filter(g->g.contains(id)).allMatch(g->g.stream().anyMatch(completed::contains))?State.ALTERNATIVE:State.REMAINING;}
 }
 public static String normalizeServer(String value){String s=value==null?"":value.strip().toLowerCase(Locale.ROOT);if(s.endsWith(":25565"))s=s.substring(0,s.length()-6);return s;}
 public static String duration(long ms){long sec=Math.max(0,ms)/1000;long h=sec/3600,m=sec%3600/60;return h+" ч "+m+" мин "+sec%60+" с";}
 public static final class Clock {
  private String key="";private long last=-1,remainder;
  public long advance(String next,long now){long elapsed=last<0?0:now-last;long delta=0;if(key.equals(next)&&!next.isEmpty()&&elapsed>=0&&elapsed<=10_000_000_000L){long total=elapsed+remainder;delta=total/1_000_000;remainder=total%1_000_000;}else remainder=0;key=next;last=now;return delta;}
  public String key(){return key;}
 }
}
