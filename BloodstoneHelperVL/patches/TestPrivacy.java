import ru.bloodhelper.client.vl.PrivacyGuard;
import java.util.*;
import java.net.*;
public class TestPrivacy {
 public static void main(String[] args)throws Exception{
  var ids=List.of("testplayer_vl","8cd25ff9-c8fe-4f54-a957-fd635e6c6ec1","testserver.invalid:25565");
  for(String url:List.of("https://example.invalid/u/TestPlayer_VL","https://example.invalid/u/8cd25ff9-c8fe-4f54-a957-fd635e6c6ec1","https://example.invalid/icon/testserver.invalid%3A25565"))if(!PrivacyGuard.sensitive(url,ids))throw new AssertionError(url);
  if(PrivacyGuard.sensitive("https://lrclib.net/api/search?q=ordinary+song",ids))throw new AssertionError("Music blocked");
  PrivacyGuard.checkUri(URI.create("http://127.0.0.1:8080/completion"));
  try{PrivacyGuard.checkUrl("https://178.22.24.133/v1/device/register");throw new AssertionError("Registration address allowed");}catch(SecurityException expected){}
  System.out.println("PASS identity/UUID/server URL blocking, music isolation and localhost/registration tests");
 }
}
