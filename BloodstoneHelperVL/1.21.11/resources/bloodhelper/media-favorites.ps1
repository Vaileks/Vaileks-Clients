# One-shot Windows UI Automation worker. The parent owns its lifetime and deadline.
# No credentials, web requests, keyboard input, focus changes, or player launches.
param([switch]$LibraryOnly)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Console]::InputEncoding = New-Object System.Text.UTF8Encoding($false)
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
Add-Type -AssemblyName UIAutomationClient
Add-Type -AssemblyName UIAutomationTypes
Add-Type -AssemblyName WindowsBase
$favoriteNativeSource = @'
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Automation;

public sealed class BloodHelperFavoriteTrack {
    public string Source = "", Title = "", Artist = "", Album = "", Token = "";
    public bool Same(BloodHelperFavoriteTrack other) {
        return other != null && Source == other.Source && Title == other.Title && Artist == other.Artist
            && Album == other.Album && Token == other.Token;
    }
}
public sealed class BloodHelperFavoriteCandidate {
    public string Identity = "", Provider = "";
    public int Saved = -1; // -1 unknown; 0 explicit unsaved; 1 explicit saved
    public bool Unique, ExactTrack, CurrentPlayer, CanAdd;
    public object Native;
}
public sealed class BloodHelperFavoriteResult {
    public string provider = "", status = "unavailable";
    public bool canFavorite, saved, accepted;
    public long requestId;
}
public interface IBloodHelperFavoriteRuntime {
    long Now { get; }
    BloodHelperFavoriteTrack CurrentTrack();
    BloodHelperFavoriteCandidate Capture();
    bool Add(BloodHelperFavoriteCandidate expected);
    void Wait();
}
// All decisions are shared with pure fixture tests. An ADD is attempted at most once.
public static class BloodHelperFavoriteEngine {
    public static bool Valid(BloodHelperFavoriteCandidate c) {
        return c != null && c.Unique && c.ExactTrack && c.CurrentPlayer && c.Identity.Length > 0
            && (c.Provider == "spotify" || c.Provider == "yandex" || c.Provider == "soundcloud")
            && (c.Saved == 1 || c.Saved == 0 && c.CanAdd);
    }
    public static BloodHelperFavoriteResult Run(IBloodHelperFavoriteRuntime runtime,
            BloodHelperFavoriteTrack expected, long requestId, long deadline, bool add) {
        var result = new BloodHelperFavoriteResult { requestId = add ? requestId : 0 };
        if (expected == null || !Regex.IsMatch(expected.Token ?? "", "\\A[a-f0-9]{32}\\z")
            || String.IsNullOrWhiteSpace(expected.Title) || String.IsNullOrWhiteSpace(expected.Artist)
            || add && requestId <= 0 || runtime.Now >= deadline) return result;
        if (!expected.Same(runtime.CurrentTrack()) || runtime.Now >= deadline) return result;
        BloodHelperFavoriteCandidate initial = runtime.Capture();
        if (!Valid(initial) || runtime.Now >= deadline || !expected.Same(runtime.CurrentTrack())) return result;
        result.provider = initial.Provider;
        result.saved = initial.Saved == 1;
        result.canFavorite = true;
        result.status = result.saved ? "saved" : "ready";
        if (!add) return result;
        if (result.saved) { result.accepted = true; return result; }
        // Re-read the same unique bottom-player button, never a stale list-row reference.
        BloodHelperFavoriteCandidate current = runtime.Capture();
        if (!Valid(current) || current.Identity != initial.Identity || current.Provider != initial.Provider
                || runtime.Now + 750 >= deadline || !expected.Same(runtime.CurrentTrack())) {
            result.canFavorite = false; result.status = "unavailable"; return result;
        }
        if (current.Saved == 1) { result.saved = true; result.accepted = true; result.status = "saved"; return result; }
        if (current.Saved != 0 || !current.CanAdd || !runtime.Add(current)) {
            result.canFavorite = false; result.status = "not_confirmed"; return result;
        }
        result.canFavorite = false;
        result.status = "not_confirmed";
        while (runtime.Now < deadline) {
            if (!expected.Same(runtime.CurrentTrack()) || runtime.Now >= deadline) return result;
            BloodHelperFavoriteCandidate after = runtime.Capture();
            // React may replace the heart or briefly remove it while applying the save.
            // Only confirmation may follow a replacement; the action above is never retried.
            if (after != null && (!after.Unique || !after.ExactTrack || !after.CurrentPlayer
                    || after.Provider != current.Provider)) return result;
            if (Valid(after) && after.Saved == 1 && runtime.Now < deadline && expected.Same(runtime.CurrentTrack())) {
                result.saved = result.accepted = result.canFavorite = true;
                result.status = "saved"; return result;
            }
            runtime.Wait();
        }
        return result;
    }
}

public sealed class BloodHelperFavoriteUia : IBloodHelperFavoriteRuntime {
    private readonly BloodHelperFavoriteTrack expected;
    private readonly Func<BloodHelperFavoriteTrack> freshTrack;
    private readonly long deadline;
    private readonly int parentId;
    private readonly long parentStarted;
    public BloodHelperFavoriteUia(BloodHelperFavoriteTrack track, Func<BloodHelperFavoriteTrack> fresh, long until, int ownerPid, long ownerStarted) {
        expected = track; freshTrack = fresh; deadline = until; parentId = ownerPid; parentStarted = ownerStarted;
    }
    public long Now { get { return (long)(Stopwatch.GetTimestamp() * 1000.0 / Stopwatch.Frequency); } }
    private bool ParentAlive() {
        if (parentId <= 0 || parentStarted <= 0) return false;
        try {
            using (Process process = Process.GetProcessById(parentId))
                return !process.HasExited && process.StartTime.ToUniversalTime().Ticks == parentStarted;
        } catch { return false; }
    }
    public BloodHelperFavoriteTrack CurrentTrack() { return Now >= deadline || !ParentAlive() ? null : freshTrack(); }
    public IDisposable StartLifetimeGuard() {
        // A blocked third-party UIA call cannot run this worker past its lease, even if
        // Java has force-killed the bridge. This timer lives only in the disposable child.
        return new Timer(ignored => {
            if (Now < deadline && ParentAlive()) return;
            try { using (Process self = Process.GetCurrentProcess()) self.Kill(); } catch { }
        }, null, 100, 100);
    }
    public void Wait() { Thread.Sleep(80); }
    public static string SourceProvider(string source) {
        if (Regex.IsMatch(source ?? "", "\\A(?:spotify(?:\\.exe)?|SpotifyAB\\.SpotifyMusic_[a-z0-9]+!Spotify)\\z", RegexOptions.IgnoreCase)) return "spotify";
        if (Regex.IsMatch(source ?? "", "\\A(?:yandexmusic(?:\\.exe)?|yandex music(?:\\.exe)?|Яндекс Музыка(?:\\.exe)?|ru\\.yandex\\.desktop\\.music|ru\\.yandex\\.music\\.desktop|com\\.squirrel\\.yandexmusic\\.yandexmusic)\\z", RegexOptions.IgnoreCase)) return "yandex";
        if (Regex.IsMatch(source ?? "", "\\A(?:soundcloud(?:\\.exe)?|SoundCloud\\.SoundCloud_[a-z0-9]+!App)\\z", RegexOptions.IgnoreCase)) return "soundcloud";
        // A PWA still needs its exact SoundCloud document URL and standalone window below.
        if (Regex.IsMatch(source ?? "", "\\A(?:Chrome|MSEdge)\\._crx_[a-p]{32}\\z", RegexOptions.IgnoreCase)) return "pwa";
        return "";
    }
    private static bool ProcessMatches(string provider, int pid) {
        try {
            using (Process process = Process.GetProcessById(pid)) {
                string name = process.ProcessName;
                if (provider == "spotify") return name.Equals("Spotify", StringComparison.OrdinalIgnoreCase);
                if (provider == "yandex") return name.Equals("YandexMusic", StringComparison.OrdinalIgnoreCase)
                    || name.Equals("Yandex Music", StringComparison.OrdinalIgnoreCase) || name.Equals("Яндекс Музыка", StringComparison.OrdinalIgnoreCase);
                if (provider == "soundcloud") return name.Equals("SoundCloud", StringComparison.OrdinalIgnoreCase);
                if (provider == "pwa") return name.Equals("chrome", StringComparison.OrdinalIgnoreCase) || name.Equals("msedge", StringComparison.OrdinalIgnoreCase);
            }
        } catch { }
        return false;
    }
    private static bool SoundCloudDocument(List<AutomationElement> nodes) {
        int matches = 0;
        foreach (AutomationElement node in nodes) {
            if (node.Current.ControlType != ControlType.Document) continue;
            object pattern;
            if (!node.TryGetCurrentPattern(ValuePattern.Pattern, out pattern)) continue;
            Uri address;
            if (Uri.TryCreate(((ValuePattern)pattern).Current.Value, UriKind.Absolute, out address)
                    && address.Scheme == "https" && (address.Host == "soundcloud.com" || address.Host == "www.soundcloud.com")) matches++;
        }
        return matches == 1;
    }
    // Chrome/Edge PWA windows must belong to the exact GSMTC app ID. A SoundCloud
    // document in some other browser window is not proof of the selected session.
    private static string WindowAppId(AutomationElement window) {
        PropertyStore store = null;
        PropVariant value = new PropVariant();
        try {
            int hwnd = window.Current.NativeWindowHandle;
            if (hwnd == 0) return "";
            Guid iid = typeof(PropertyStore).GUID;
            if (SHGetPropertyStoreForWindow(new IntPtr(hwnd), ref iid, out store) != 0 || store == null) return "";
            var key = new PropertyKey { Format = new Guid("9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3"), Id = 5 };
            if (store.GetValue(ref key, out value) != 0 || value.Type != 31 || value.Pointer == IntPtr.Zero) return "";
            return System.Runtime.InteropServices.Marshal.PtrToStringUni(value.Pointer) ?? "";
        } catch { return ""; }
        finally {
            PropVariantClear(ref value);
            if (store != null) System.Runtime.InteropServices.Marshal.ReleaseComObject(store);
        }
    }
    [System.Runtime.InteropServices.StructLayout(System.Runtime.InteropServices.LayoutKind.Sequential)]
    private struct PropertyKey { internal Guid Format; internal uint Id; }
    [System.Runtime.InteropServices.StructLayout(System.Runtime.InteropServices.LayoutKind.Explicit, Size = 24)]
    private struct PropVariant {
        [System.Runtime.InteropServices.FieldOffset(0)] internal ushort Type;
        [System.Runtime.InteropServices.FieldOffset(8)] internal IntPtr Pointer;
    }
    [System.Runtime.InteropServices.ComImport, System.Runtime.InteropServices.Guid("886d8eeb-8cf2-4446-8d02-cdba1dbdcf99"),
        System.Runtime.InteropServices.InterfaceType(System.Runtime.InteropServices.ComInterfaceType.InterfaceIsIUnknown)]
    private interface PropertyStore {
        [System.Runtime.InteropServices.PreserveSig] int GetCount(out uint count);
        [System.Runtime.InteropServices.PreserveSig] int GetAt(uint index, out PropertyKey key);
        [System.Runtime.InteropServices.PreserveSig] int GetValue(ref PropertyKey key, out PropVariant value);
        [System.Runtime.InteropServices.PreserveSig] int SetValue(ref PropertyKey key, ref PropVariant value);
        [System.Runtime.InteropServices.PreserveSig] int Commit();
    }
    [System.Runtime.InteropServices.DllImport("shell32.dll")]
    private static extern int SHGetPropertyStoreForWindow(IntPtr hwnd, ref Guid iid, out PropertyStore store);
    [System.Runtime.InteropServices.DllImport("ole32.dll")]
    private static extern int PropVariantClear(ref PropVariant value);
    private List<AutomationElement> Walk(AutomationElement root, int maximum) {
        var result = new List<AutomationElement>();
        var pending = new Stack<KeyValuePair<AutomationElement, int>>();
        bool truncated = false;
        pending.Push(new KeyValuePair<AutomationElement,int>(root, 0));
        while (pending.Count > 0 && result.Count < maximum && Now < deadline) {
            var entry = pending.Pop();
            result.Add(entry.Key);
            // Depth-first document order preserves collaborators even with different wrappers.
            AutomationElement child = TreeWalker.ControlViewWalker.GetLastChild(entry.Key);
            if (entry.Value >= 40) { if (child != null) truncated = true; continue; }
            while (child != null && result.Count + pending.Count < maximum && Now < deadline) {
                pending.Push(new KeyValuePair<AutomationElement,int>(child, entry.Value + 1));
                child = TreeWalker.ControlViewWalker.GetPreviousSibling(child);
            }
            if (child != null) truncated = true;
        }
        // A truncated tree cannot prove uniqueness.
        return !truncated && pending.Count == 0 && Now < deadline ? result : null;
    }
    private static string DisplayText(string value) {
        // UI labels can contain nonbreaking spaces or decomposed accents. Keep punctuation,
        // case and edition suffixes: a remix must not match the original recording.
        return Regex.Replace((value ?? "").Normalize(System.Text.NormalizationForm.FormC), @"\s+", " ").Trim();
    }
    public static bool TextIs(string actual, string expected) { return DisplayText(actual) == DisplayText(expected); }
    public static bool SpotifyArtistsMatch(string expectedArtist, IEnumerable<string> accessibleLinkNames) {
        var names = new List<string>();
        foreach (string label in accessibleLinkNames) {
            if (names.Count >= 64) return false;
            names.Add(DisplayText(label));
        }
        string expected = DisplayText(expectedArtist);
        if (expected.Length == 0) return false;
        // Spotify exposes separate artist links. Match a complete contiguous sequence in
        // the current player's links, never a substring of an artist or only one co-author.
        foreach (string separator in new [] { ", ", "; ", " · " }) {
            for (int start = 0; start < names.Count; start++) {
                string joined = "";
                for (int end = start; end < names.Count; end++) {
                    if (names[end].Length == 0) break;
                    joined += (end == start ? "" : separator) + names[end];
                    if (joined == expected) return true;
                    if (joined.Length >= expected.Length) break;
                }
            }
        }
        return false;
    }
    public static bool YandexArtistsMatch(string expectedArtist, IEnumerable<string> accessibleLinkNames) {
        // Installed Yandex publishes artists.map(name).join(", ") to GSMTC, while
        // its player exposes individual links as "Artist {name}" / "Артист {name}".
        // Match the complete ordered sequence, never a substring or one collaborator.
        var names = new List<string>();
        foreach (string label in accessibleLinkNames) {
            string name = null;
            if (label != null && label.StartsWith("Артист ", StringComparison.Ordinal)) name = label.Substring(7);
            else if (label != null && label.StartsWith("Artist ", StringComparison.Ordinal)) name = label.Substring(7);
            if (String.IsNullOrWhiteSpace(name)) return false;
            names.Add(name);
        }
        return names.Count > 0 && TextIs(String.Join(", ", names), expectedArtist);
    }
    public static bool IsPlay(string label) {
        return Regex.IsMatch(label ?? "", "\\A(?:Play|Playback|Pause|Resume|Воспроизвести|Воспроизведение|Играть|Пауза|Продолжить)(?:\\s*\\([^)]*\\))?\\z", RegexOptions.IgnoreCase);
    }
    public static bool IsSkip(string label) {
        return Regex.IsMatch(label ?? "", "\\A(?:Next|Previous|Next track|Previous track|Next song|Previous song|Skip to next|Skip to previous|Следующий трек|Предыдущий трек|Следующая песня|Предыдущая песня|Далее|Назад)(?:\\s*\\([^)]*\\))?\\z", RegexOptions.IgnoreCase);
    }
    public static bool LikeLabel(string label, bool spotifyPlayer) {
        return Regex.IsMatch(DisplayText(label), "\\A(?:Like|Unlike|Like track|Unlike track|Нравится|Больше не нравится|Мне нравится|В избранное|Добавить в избранное|Удалить из избранного|Добавить в любимые|Удалить из любимого|Save to (?:your )?Liked Songs|Add to (?:your )?Liked Songs|Remove from (?:your )?Liked Songs|Добавить в любимые треки|Удалить из любимых треков)\\z", RegexOptions.IgnoreCase)
            || spotifyPlayer && LibraryLabelState(label) >= 0;
    }
    private static int LibraryLabelState(string label) {
        // Spotify's current track toggle (xpui module 93845) uses the Library label and
        // aria-checked, even though it saves a song to Liked Songs. Never apply to list rows.
        string name = DisplayText(label);
        if (Regex.IsMatch(name, @"\A(?:(?:Save|Add) to (?:your )?Library|Добавить в медиатеку)\z", RegexOptions.IgnoreCase)) return 0;
        if (Regex.IsMatch(name, @"\A(?:Remove from (?:your )?Library|Удалить из медиатеки)\z", RegexOptions.IgnoreCase)) return 1;
        return -1;
    }
    public static int LabelState(string label, bool spotifyPlayer) {
        string name = DisplayText(label);
        if (Regex.IsMatch(name, @"\A(?:Unlike(?: track)?|Больше не нравится|Удалить из (?:избранного|любимого|любимых треков)|Remove from (?:your )?Liked Songs)\z", RegexOptions.IgnoreCase)) return 1;
        if (Regex.IsMatch(name, @"\A(?:Добавить в (?:избранное|любимые|любимые треки)|(?:Save|Add) to (?:your )?Liked Songs)\z", RegexOptions.IgnoreCase)) return 0;
        return spotifyPlayer ? LibraryLabelState(name) : -1;
    }
    public static int AriaState(string value) {
        bool saved = Regex.IsMatch(value ?? "", @"(?:\A|;)\s*(?:pressed|checked)=true\s*(?:;|\z)", RegexOptions.IgnoreCase);
        bool unsaved = Regex.IsMatch(value ?? "", @"(?:\A|;)\s*(?:pressed|checked)=false\s*(?:;|\z)", RegexOptions.IgnoreCase);
        return saved == unsaved ? -1 : saved ? 1 : 0;
    }
    private static string AriaProperties(AutomationElement button) {
        AutomationProperty aria = AutomationProperty.LookupById(30102);
        return aria == null ? "" : button.GetCurrentPropertyValue(aria, true) as string ?? "";
    }
    private static bool LikeButton(AutomationElement button, bool spotifyPlayer) {
        return LikeLabel(button.Current.Name, spotifyPlayer)
            // A playlist picker is not an idempotent track-like button.
            && !Regex.IsMatch(AriaProperties(button), @"(?:\A|;)\s*haspopup=(?:true|menu|dialog|listbox)\s*(?:;|\z)", RegexOptions.IgnoreCase);
    }
    public static int ResolveState(string label, string aria, int toggle) {
        int named = LabelState(label, true), accessible = AriaState(aria);
        if (accessible < 0 && Regex.IsMatch(aria ?? "", @"(?:\A|;)\s*(?:pressed|checked)=", RegexOptions.IgnoreCase)) return -1;
        if (toggle >= 0 && accessible >= 0 && toggle != accessible
                || named >= 0 && accessible >= 0 && named != accessible
                || named >= 0 && toggle >= 0 && named != toggle) return -1;
        return toggle >= 0 ? toggle : accessible >= 0 ? accessible : named;
    }
    private static int State(AutomationElement button) {
        object pattern;
        int toggle = -1;
        if (button.TryGetCurrentPattern(TogglePattern.Pattern, out pattern)) {
            ToggleState state = ((TogglePattern)pattern).Current.ToggleState;
            if (state == ToggleState.Indeterminate) return -1;
            toggle = state == ToggleState.On ? 1 : 0;
        }
        // This method is reached only after the scoped LikeButton check.
        return ResolveState(button.Current.Name, AriaProperties(button), toggle);
    }
    private static string Identity(AutomationElement node) {
        int[] ids = node.GetRuntimeId();
        return node.Current.ProcessId + ":" + String.Join(".", Array.ConvertAll(ids, value => value.ToString(System.Globalization.CultureInfo.InvariantCulture)));
    }
    public BloodHelperFavoriteCandidate Capture() {
        string sourceProvider = SourceProvider(expected.Source);
        if (sourceProvider.Length == 0 || Now >= deadline) return null;
        var found = new Dictionary<string, BloodHelperFavoriteCandidate>();
        AutomationElementCollection windows = AutomationElement.RootElement.FindAll(TreeScope.Children, Condition.TrueCondition);
        foreach (AutomationElement window in windows) {
            if (Now >= deadline) return null;
            try {
                if (!ProcessMatches(sourceProvider, window.Current.ProcessId) || window.Current.IsOffscreen || !window.Current.IsEnabled) continue;
                var windowRect = window.Current.BoundingRectangle;
                if (windowRect.Width < 320 || windowRect.Height < 200) continue;
                AutomationElement scanRoot = window;
                bool spotifyPlayer = false;
                if (sourceProvider == "spotify") {
                    // Use Spotify's actual landmark, not the entire album/playlist tree.
                    // A long playlist used to exceed the scan limit and disable its heart.
                    var players = window.FindAll(TreeScope.Descendants, new OrCondition(
                        new PropertyCondition(AutomationElement.NameProperty, "Now playing bar"),
                        new PropertyCondition(AutomationElement.NameProperty, "Панель «Сейчас играет»")));
                    AutomationElement visible = null;
                    foreach (AutomationElement player in players) {
                        if (player.Current.IsOffscreen || !player.Current.IsEnabled) continue;
                        if (visible != null) return null;
                        visible = player;
                    }
                    if (visible != null) { scanRoot = visible; spotifyPlayer = true; }
                }
                if (sourceProvider == "yandex") {
                    // Yandex album pages exceed 20 accessibility levels. Read the current player only,
                    // so offscreen album rows and recommendation cards cannot invalidate its heart.
                    var players = window.FindAll(TreeScope.Descendants, new AndCondition(
                        new PropertyCondition(AutomationElement.ControlTypeProperty, ControlType.Group),
                        new OrCondition(new PropertyCondition(AutomationElement.NameProperty, "Плеер"),
                            new PropertyCondition(AutomationElement.NameProperty, "Player"))));
                    AutomationElement visible = null;
                    foreach (AutomationElement player in players) {
                        if (player.Current.IsOffscreen || !player.Current.IsEnabled) continue;
                        if (visible != null) return null;
                        visible = player;
                    }
                    if (visible == null) continue;
                    scanRoot = visible;
                }
                var nodes = Walk(scanRoot, sourceProvider == "yandex" || spotifyPlayer ? 400 : 2400);
                if (nodes == null) return null;
                if (sourceProvider == "pwa") {
                    if (!WindowAppId(window).Equals(expected.Source, StringComparison.OrdinalIgnoreCase)
                            || !(window.Current.Name ?? "").Contains("SoundCloud") || !SoundCloudDocument(nodes)) continue;
                    // Browser tabs/address bars distinguish a normal browser from a standalone PWA.
                    bool browserChrome = false;
                    foreach (AutomationElement node in nodes)
                        if (node.Current.ControlType == ControlType.Tab || node.Current.AutomationId == "addressEditBox") browserChrome = true;
                    if (browserChrome) continue;
                }
                foreach (AutomationElement button in nodes) {
                    if (button.Current.ControlType != ControlType.Button || button.Current.IsOffscreen || !button.Current.IsEnabled || !LikeButton(button, spotifyPlayer)) continue;
                    AutomationElement scope = TreeWalker.ControlViewWalker.GetParent(button);
                    for (int depth = 0; scope != null && depth < 12 && !scope.Equals(window);
                            depth++, scope = scope.Equals(scanRoot) ? null : TreeWalker.ControlViewWalker.GetParent(scope)) {
                        var rect = scope.Current.BoundingRectangle;
                        if (rect.IsEmpty || rect.Width < 200 || rect.Height < 24 || rect.Height > windowRect.Height * .40
                                || rect.Top < windowRect.Top + windowRect.Height * .55 || rect.Bottom < windowRect.Bottom - 180) continue;
                        List<AutomationElement> player = Walk(scope, 400);
                        if (player == null) continue;
                        // New Yandex versions hide track labels from the player accessibility subtree.
                        // Its unique named PlayerBar is bound to the fresh GSMTC identity by the engine
                        // before/after both captures and once more immediately before the add operation.
                        bool identifiedYandexPlayer = sourceProvider == "yandex" && scope.Equals(scanRoot)
                            && (scope.Current.ClassName ?? "").Contains("PlayerBar");
                        bool title = identifiedYandexPlayer, artist = identifiedYandexPlayer, play = false, skip = false;
                        var yandexArtists = new List<string>();
                        var spotifyLinks = new List<string>();
                        int favorites = 0;
                        foreach (AutomationElement node in player) {
                            if (node.Current.IsOffscreen) continue;
                            string name = node.Current.Name ?? "";
                            title |= TextIs(name, expected.Title);
                            artist |= TextIs(name, expected.Artist);
                            if (sourceProvider == "yandex" && node.Current.ControlType == ControlType.Hyperlink
                                    && (name.StartsWith("Артист ", StringComparison.Ordinal) || name.StartsWith("Artist ", StringComparison.Ordinal)))
                                yandexArtists.Add(name);
                            if (sourceProvider == "spotify" && node.Current.ControlType == ControlType.Hyperlink)
                                spotifyLinks.Add(name);
                            if (node.Current.ControlType == ControlType.Button) {
                                play |= IsPlay(name); skip |= IsSkip(name);
                                if (LikeButton(node, spotifyPlayer)) favorites++;
                            }
                        }
                        if (sourceProvider == "yandex") artist |= YandexArtistsMatch(expected.Artist, yandexArtists);
                        if (sourceProvider == "spotify") artist |= SpotifyArtistsMatch(expected.Artist, spotifyLinks);
                        if (!title || !artist || !play || !skip || favorites != 1) continue;
                        object ignored;
                        string id = Identity(button);
                        found[id] = new BloodHelperFavoriteCandidate { Identity = id, Provider = sourceProvider == "pwa" ? "soundcloud" : sourceProvider,
                            Unique = true, ExactTrack = true, CurrentPlayer = true, Saved = State(button), Native = button,
                            CanAdd = button.TryGetCurrentPattern(InvokePattern.Pattern, out ignored) || button.TryGetCurrentPattern(TogglePattern.Pattern, out ignored) };
                    }
                }
            } catch (ElementNotAvailableException) { return null; }
            catch (InvalidOperationException) { return null; }
        }
        if (found.Count != 1 || Now >= deadline) return null;
        foreach (var value in found.Values) return value;
        return null;
    }
    public bool Add(BloodHelperFavoriteCandidate expectedButton) {
        // No cached pattern survives this final check. Never toggle On or unknown state.
        if (Now + 500 >= deadline || !expected.Same(CurrentTrack())) return false;
        var button = expectedButton.Native as AutomationElement;
        if (button == null || Identity(button) != expectedButton.Identity || !button.Current.IsEnabled || button.Current.IsOffscreen
                || !LikeButton(button, expectedButton.Provider == "spotify") || State(button) != 0) return false;
        object pattern;
        if (button.TryGetCurrentPattern(InvokePattern.Pattern, out pattern) && Now + 100 < deadline && ParentAlive()) { ((InvokePattern)pattern).Invoke(); return true; }
        if (button.TryGetCurrentPattern(TogglePattern.Pattern, out pattern) && ((TogglePattern)pattern).Current.ToggleState == ToggleState.Off
                && Now + 100 < deadline && ParentAlive()) {
            ((TogglePattern)pattern).Toggle(); return true;
        }
        return false;
    }
}
'@
Add-Type -TypeDefinition $favoriteNativeSource -ReferencedAssemblies @('UIAutomationClient', 'UIAutomationTypes', 'WindowsBase')
if ($LibraryOnly) { return }

$result = @{ provider = ''; canFavorite = $false; saved = $false; requestId = [long]0; accepted = $false; status = 'unavailable' }
try {
    $line = [Console]::In.ReadLine()
    if ($null -eq $line -or $line.Length -gt 16384) { throw 'Invalid request' }
    $request = $line | ConvertFrom-Json
    if ($request.mode -cne 'observe' -and $request.mode -cne 'add') { throw 'Invalid mode' }
    $track = New-Object BloodHelperFavoriteTrack
    $track.Source = [string]$request.source; $track.Title = [string]$request.title
    $track.Artist = [string]$request.artist; $track.Album = [string]$request.album; $track.Token = [string]$request.trackToken
    $requestId = [long]$request.requestId
    if ($request.mode -ceq 'add' -and $requestId -gt 0) { $result.requestId = $requestId }
    $deadline = [long]$request.deadline
    $now = [long]([Diagnostics.Stopwatch]::GetTimestamp() * 1000.0 / [Diagnostics.Stopwatch]::Frequency)
    if ($deadline -le $now -or $deadline -gt $now + 9000) { throw 'Expired request' }
    if ([BloodHelperFavoriteUia]::SourceProvider($track.Source) -ceq '') { $result.status = 'unsupported'; throw 'Unsupported source' }
    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $managerType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
    $propertiesType = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType = WindowsRuntime]
    $asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
        $_.Name -eq 'AsTask' -and $_.IsGenericMethodDefinition -and $_.GetGenericArguments().Length -eq 1 -and
            $_.GetParameters().Length -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
    } | Select-Object -First 1
    function Read-FavoriteWinRT($operation, [Type]$type) {
        $task = $asTask.MakeGenericMethod($type).Invoke($null, @($operation))
        if (-not $task.Wait(450)) { try { $operation.Cancel() } catch {}; throw 'Expired media observation' }
        return ,$task.Result
    }
    . (Join-Path $PSScriptRoot 'media-sources.ps1')
    $manager = Read-FavoriteWinRT ($managerType::RequestAsync()) $managerType
    $script:favoriteWorkerSessionIdentity = ''
    function Get-FavoriteSessionIdentity($session) {
        $unknown = [IntPtr]::Zero
        try {
            if ($null -eq $session) { return '' }
            $unknown = [Runtime.InteropServices.Marshal]::GetIUnknownForObject($session)
            return $unknown.ToInt64().ToString('x')
        } finally { if ($unknown -ne [IntPtr]::Zero) { $null = [Runtime.InteropServices.Marshal]::Release($unknown) } }
    }
    $fresh = [Func[BloodHelperFavoriteTrack]] {
        try {
            $session = (Select-MediaSession $manager ([string]$request.preferredSource))
            if ($null -eq $session) { return $null }
            $identity = Get-FavoriteSessionIdentity $session
            if ($identity.Length -eq 0 -or ($script:favoriteWorkerSessionIdentity.Length -gt 0 -and $script:favoriteWorkerSessionIdentity -cne $identity)) { return $null }
            $script:favoriteWorkerSessionIdentity = $identity
            $properties = Read-FavoriteWinRT ($session.TryGetMediaPropertiesAsync()) $propertiesType
            if ((Get-FavoriteSessionIdentity ((Select-MediaSession $manager ([string]$request.preferredSource)))) -cne $identity) { return $null }
            $current = New-Object BloodHelperFavoriteTrack
            $current.Source = [string]$session.SourceAppUserModelId
            $current.Title = [string]$properties.Title; $current.Artist = [string]$properties.Artist
            $current.Album = [string]$properties.AlbumTitle; $current.Token = $track.Token
            return $current
        } catch { return $null }
    }
    $runtime = New-Object BloodHelperFavoriteUia($track, $fresh, $deadline, [int]$request.parentProcessId, [long]$request.parentStartedTicks)
    $lifetimeGuard = $runtime.StartLifetimeGuard()
    $result = [BloodHelperFavoriteEngine]::Run($runtime, $track, $requestId, $deadline, ($request.mode -ceq 'add'))
} catch {
    # The parent reports a conservative unavailable result, never exception details or metadata.
} finally {
    if ($null -ne $lifetimeGuard) { $lifetimeGuard.Dispose() }
}
[Console]::Out.WriteLine(($result | ConvertTo-Json -Compress -Depth 3))
[Console]::Out.Flush()
