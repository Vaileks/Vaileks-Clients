# Explicit search worker: UI Automation targets Spotify only. No credentials, clipboard, keystrokes or focus changes.
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Console]::InputEncoding = New-Object Text.UTF8Encoding($false)
[Console]::OutputEncoding = New-Object Text.UTF8Encoding($false)
$answer = @{status='unavailable';tracks=@()}
try {
    $line=[Console]::In.ReadLine()
    if($null -eq $line -or $line.Length -gt 8192){throw 'Invalid request'}
    $request=$line | ConvertFrom-Json
    if($request.mode -ceq 'open_yandex') {
        if([string]$request.id -cnotmatch '\A[0-9]{1,20}:[0-9]{1,20}\z'){throw 'Invalid track'}
        $ids=([string]$request.id).Split(':')
        $prefix=if(Test-Path 'Registry::HKEY_CLASSES_ROOT\yandexmusic'){'yandexmusic://'}else{'https://music.yandex.ru/'}
        Start-Process -FilePath ($prefix+'album/'+$ids[1]+'/track/'+$ids[0])
        $answer.status='opened'
    } elseif($request.mode -ceq 'search' -or $request.mode -ceq 'play' -or $request.mode -ceq 'play_spotify_uri') {
        if($request.mode -ceq 'play_spotify_uri'){
            if([string]$request.id -cnotmatch '\Aspotify:track:[A-Za-z0-9]{22}\z'){throw 'Invalid Spotify track'}
            $request.query=[string]$request.title+' '+[string]$request.artist
        }
        if([string]::IsNullOrWhiteSpace($request.query) -or $request.query.Length -gt 240 -or $request.query -match '[\x00-\x1f]'){throw 'Invalid query'}
        Add-Type -AssemblyName UIAutomationClient
        Add-Type -AssemblyName UIAutomationTypes
        Add-Type -AssemblyName WindowsBase
        $code=@'
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Automation;

public sealed class BloodHelperSearchArtist { public string name="",uri=""; }
public sealed class BloodHelperSearchTrack {
    public BloodHelperSearchArtist[] artists=new BloodHelperSearchArtist[0];
    public string id="",title="",artist="",uri="";
    public long durationMs;
    internal AutomationElement button;
}
public sealed class BloodHelperSearchReply {
    public string status="unavailable";
    public BloodHelperSearchTrack[] tracks=new BloodHelperSearchTrack[0];
}
public sealed class BloodHelperSpotifySearch {
    readonly Stopwatch clock=Stopwatch.StartNew();
    readonly int ownerPid;
    readonly long ownerStarted;
    int spotifyPid;
    long spotifyStarted;
    AutomationElement window;
    static readonly TreeWalker walker=TreeWalker.ControlViewWalker;
    public BloodHelperSpotifySearch(int owner) {
        ownerPid=owner;
        using(var p=Process.GetProcessById(owner))ownerStarted=p.StartTime.ToUniversalTime().Ticks;
    }
    bool Alive() {
        try {using(var p=Process.GetProcessById(ownerPid))return !p.HasExited&&p.StartTime.ToUniversalTime().Ticks==ownerStarted;}
        catch{return false;}
    }
    bool Valid(){return clock.ElapsedMilliseconds<9500&&Alive();}
    public IDisposable Lease() {
        return new Timer(_=>{if(Valid())return;try{using(var p=Process.GetCurrentProcess())p.Kill();}catch{}},null,100,100);
    }
    bool TargetAlive() {
        try {using(var p=Process.GetProcessById(spotifyPid))return Valid()&&!p.HasExited&&p.ProcessName.Equals("Spotify",StringComparison.OrdinalIgnoreCase)
            &&p.StartTime.ToUniversalTime().Ticks==spotifyStarted;}
        catch{return false;}
    }
    List<AutomationElement> Walk(AutomationElement root,int max) {
        var result=new List<AutomationElement>();
        var queue=new Queue<KeyValuePair<AutomationElement,int>>();queue.Enqueue(new KeyValuePair<AutomationElement,int>(root,0));
        while(queue.Count>0&&result.Count<max&&Valid()) {
            var item=queue.Dequeue();result.Add(item.Key);
            var child=walker.GetFirstChild(item.Key);
            if(item.Value>=40&&child!=null)return null;
            while(child!=null) {
                if(queue.Count+result.Count>=max||!Valid())return null;
                queue.Enqueue(new KeyValuePair<AutomationElement,int>(child,item.Value+1));child=walker.GetNextSibling(child);
            }
        }
        return queue.Count==0&&Valid()?result:null;
    }
    static string Name(AutomationElement node){return node.Current.Name??"";}
    static bool Visible(AutomationElement node){return node.Current.IsEnabled&&!node.Current.IsOffscreen;}
    static bool SearchName(string name) {return Regex.IsMatch(name,@"\A(?:Search(?: for (?:songs|music|artists|content).*)?|Поиск(?: треков.*)?|Что (?:вы )?хочешь(?:ете)? (?:послушать|слушать)\??|Что вы хотите послушать\??|What do you want to (?:listen to|play)\??)\z",RegexOptions.IgnoreCase);}
    static AutomationElement SearchField(List<AutomationElement> nodes) {
        if(nodes==null)return null;
        var fields=nodes.Where(n=>n.Current.ControlType==ControlType.Edit&&Visible(n)&&SearchName(Name(n))
            &&n.Current.AutomationId!="view_1012"&&n.Current.ClassName!="OmniboxViewViews").ToList();
        return fields.Count==1?fields[0]:null;
    }
    static string Value(AutomationElement field) {
        object value;if(field==null||!field.TryGetCurrentPattern(ValuePattern.Pattern,out value))return null;
        return ((ValuePattern)value).Current.Value;
    }
    static bool Invoke(AutomationElement node) {
        object pattern;if(!Visible(node)||!node.TryGetCurrentPattern(InvokePattern.Pattern,out pattern))return false;
        ((InvokePattern)pattern).Invoke();return true;
    }
    string Key(AutomationElement button,string title,string artist,long duration,string query) {
        var identity=spotifyPid+":"+spotifyStarted+":"+String.Join(".",button.GetRuntimeId())+":"+query+":"+title+":"+artist+":"+duration;
        using(var sha=SHA256.Create())return BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(identity))).Replace("-","").ToLowerInvariant();
    }
    bool InSearchResults(AutomationElement row) {
        for(var node=walker.GetParent(row);node!=null;node=walker.GetParent(node)) {
            string cls=node.Current.ClassName??"";
            if(cls.IndexOf("searchPage",StringComparison.OrdinalIgnoreCase)>=0||cls.IndexOf("searchResults",StringComparison.OrdinalIgnoreCase)>=0)return true;
            if(node.Current.ControlType==ControlType.Window)break;
        }
        return false;
    }
    List<BloodHelperSearchTrack> Capture(List<AutomationElement> nodes,string query) {
        var results=new List<BloodHelperSearchTrack>();
        if(nodes==null||Value(SearchField(nodes))!=query||!TargetAlive())return results;
        foreach(var row in nodes) {
            string cls=row.Current.ClassName??"";
            if(!Visible(row)||cls.IndexOf("trackListRow",StringComparison.OrdinalIgnoreCase)<0||!InSearchResults(row))continue;
            var children=Walk(row,180);if(children==null)continue;
            var links=children.Where(n=>n.Current.ControlType==ControlType.Hyperlink&&Visible(n)&&!String.IsNullOrWhiteSpace(Name(n))).ToList();
            if(links.Count<2)continue;
            string title=Name(links[0]),artist=Name(links[1]);
            if(title.Length>300||artist.Length>300)continue;
            // Only a row's explicitly named Play button is actionable; never its menu/like controls.
            var buttons=children.Where(n=>n.Current.ControlType==ControlType.Button&&Visible(n)
                &&Regex.IsMatch(Name(n),@"\A(?:Play|Воспроизвести|Слушать)(?:\s|$)",RegexOptions.IgnoreCase)).ToList();
            if(buttons.Count!=1)continue;
            long duration=0;
            foreach(var node in children) {
                var match=Regex.Match(Name(node),@"\A([0-9]{1,3}):([0-5][0-9])\z");
                if(match.Success)duration=(Int64.Parse(match.Groups[1].Value)*60+Int64.Parse(match.Groups[2].Value))*1000;
            }
            if(duration==0)continue; // Album/artist/playlist cards are not song results.
            var track=new BloodHelperSearchTrack{title=title,artist=artist,durationMs=duration,button=buttons[0]};
            track.id=Key(track.button,title,artist,duration,query);
            string link=Value(links[0]);
            var songUri=Regex.Match(link??"",@"\A(?:https://open\.spotify\.com/track/|spotify:track:)([A-Za-z0-9]{22})(?:\?.*)?\z");
            if(songUri.Success)track.uri="spotify:track:"+songUri.Groups[1].Value;
            var people=new List<BloodHelperSearchArtist>();
            foreach(var person in links.Skip(1)) {
                var match=Regex.Match(Value(person)??"",@"\A(?:https://open\.spotify\.com/artist/|spotify:artist:)([A-Za-z0-9]{22})(?:\?.*)?\z");
                if(match.Success)people.Add(new BloodHelperSearchArtist{name=Name(person),uri="spotify:artist:"+match.Groups[1].Value});
                if(people.Count==12)break;
            }
            track.artists=people.ToArray();
            if(people.Count>0)track.artist=String.Join(", ",people.Select(p=>p.name));
            if(!results.Any(t=>t.id==track.id))results.Add(track);
            if(results.Count>=20)break;
        }
        return results;
    }
    public BloodHelperSearchReply PlayCatalog(string query,string uri,string title,string artist) {
        var found=Run(query,"",false);
        if(found.status!="ready")return found;
        var chosen=found.tracks.Where(t=>t.uri==uri).ToList();
        if(chosen.Count!=1){return new BloodHelperSearchReply{status="stale"};}
        return Run(query,chosen[0].id,true);
    }
    public BloodHelperSearchReply Run(string query,string id,bool play) {
        var answer=new BloodHelperSearchReply();
        var candidates=new List<AutomationElement>();
        foreach(AutomationElement w in AutomationElement.RootElement.FindAll(TreeScope.Children,Condition.TrueCondition)) {
            if(!Valid())return answer;
            try {using(var p=Process.GetProcessById(w.Current.ProcessId)) {
                if(p.ProcessName.Equals("Spotify",StringComparison.OrdinalIgnoreCase)&&w.Current.ControlType==ControlType.Window
                    &&Visible(w)&&w.Current.BoundingRectangle.Width>=400&&w.Current.BoundingRectangle.Height>=250)candidates.Add(w);
            }}catch{}
        }
        if(candidates.Count!=1){answer.status=candidates.Count==0?"open_player":"unavailable";return answer;}
        window=candidates[0];spotifyPid=window.Current.ProcessId;
        using(var p=Process.GetProcessById(spotifyPid))spotifyStarted=p.StartTime.ToUniversalTime().Ticks;
        var nodes=Walk(window,3000);if(nodes==null)return answer;
        var field=SearchField(nodes);
        if(field==null) {
            if(nodes.Any(n=>n.Current.ControlType==ControlType.Button&&Regex.IsMatch(Name(n),@"\A(?:Log in|Sign in|Войти)\z",RegexOptions.IgnoreCase))) {
                answer.status="sign_in";return answer;
            }
            if(play){answer.status="stale";return answer;}
            var navigation=nodes.Where(n=>(n.Current.ControlType==ControlType.Hyperlink||n.Current.ControlType==ControlType.Button)
                &&Visible(n)&&Regex.IsMatch(Name(n),@"\A(?:Search|Поиск)\z",RegexOptions.IgnoreCase)).ToList();
            if(navigation.Count!=1||!TargetAlive()||!Invoke(navigation[0]))return answer;
            Thread.Sleep(300);nodes=Walk(window,3000);field=SearchField(nodes);
        }
        if(field==null)return answer;
        if(play) {
            var tracks=Capture(nodes,query);var chosen=tracks.Where(t=>t.id==id).ToList();
            if(chosen.Count!=1){answer.status="stale";return answer;}
            // Fresh capture binds the result to process start, query, row identity and complete metadata.
            var current=Capture(Walk(window,3000),query).Where(t=>t.id==id).ToList();
            if(current.Count!=1||!TargetAlive()||Value(field)!=query){answer.status="stale";return answer;}
            if(Invoke(current[0].button))answer.status="requested";
            return answer;
        }
        object pattern;
        if(!field.TryGetCurrentPattern(ValuePattern.Pattern,out pattern)||((ValuePattern)pattern).Current.IsReadOnly||!TargetAlive())return answer;
        ((ValuePattern)pattern).SetValue(query);
        Thread.Sleep(850);
        string previous="";long stableSince=0;
        while(Valid()&&TargetAlive()) {
            nodes=Walk(window,3000);if(nodes==null)return answer;
            if(Value(SearchField(nodes))!=query){answer.status="stale";return answer;}
            var tracks=Capture(nodes,query);
            if(tracks.Count>0) {
                string key=String.Join("|",tracks.Select(t=>t.id));
                if(key!=previous){previous=key;stableSince=clock.ElapsedMilliseconds;}
                else if(clock.ElapsedMilliseconds-stableSince>=500){answer.status="ready";answer.tracks=tracks.ToArray();return answer;}
            } else if(nodes.Any(n=>Regex.IsMatch(Name(n),@"\A(?:No results found|Ничего не найдено|Ничего не удалось найти)(?:[\s.!]|$)",RegexOptions.IgnoreCase))) {
                answer.status="empty";return answer;
            }
            Thread.Sleep(200);
        }
        return answer;
    }
}
'@
        Add-Type -TypeDefinition $code -ReferencedAssemblies UIAutomationClient,UIAutomationTypes,WindowsBase
        $engine=New-Object BloodHelperSpotifySearch([int]$request.ownerPid)
        $lease=$engine.Lease()
        try{
            if($request.mode -ceq 'play_spotify_uri'){$answer=$engine.PlayCatalog([string]$request.query,[string]$request.id,[string]$request.title,[string]$request.artist)}
            else{$answer=$engine.Run([string]$request.query,[string]$request.id,($request.mode -ceq 'play'))}
        }finally{$lease.Dispose()}
    }
} catch { }
[Console]::Out.WriteLine(($answer | ConvertTo-Json -Depth 6 -Compress))
[Console]::Out.Flush()
