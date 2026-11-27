# Evolution feature adaptations

Upstream: Evolution ClickGUI 1.0.0 for Minecraft 1.21.11, author `darkich` in fabric.mod.json.
Supplied JAR SHA-256: 8e558dd15e3e727a3733f0c18d4b8e4af3b80eb8d62a89ebf32c4b8f6af90cff.
Upstream metadata: All-Rights-Reserved; no standalone LICENSE was included.

On 2026-10-16, the BloodHelper project owner stated in the project conversation
that the mod author had permitted taking some functions, in the context of the
requested WorldLyrics, AbilityTrails, and LootHighlight integration. This is the
reported permission basis for these limited adaptations; it is not a public
MIT license for Evolution, its other functionality, or its assets.

Adapted portions: visuals/TrailTracker.java, visuals/ZoneTracker.java,
visuals/AbilityTracker.java; the selection/presentation behavior in
visuals/LootHighlight.java and lyrics/KineticLyricsRenderer.java; and the hooks
LegendaryZoneMixin, LegendaryDamageMixin, LootGlowMixin, LootColorMixin.
Paths are relative to src/main/java/ru/bloodhelper/client/.

BloodHelper changes: shared existing lyric/media services and fonts, projected
word rendering, module/settings integration, independent ability/loot switches,
bounded state, world/disable resets, range and visibility checks, and regression
coverage. No Evolution party service, credentials, media scripts, assets, or
original JAR is bundled. The original-code license does not relicense these
third-party portions. Preserve this provenance and the applicable permission.


Attribution correction (project owner, 2026-10-16): credit the adapted WorldLyrics, AbilityTrails and LootHighlight features to Minions_. darkich integrated the GUI. Original archive metadata is historical identification, not the feature authorship claim. The previously reported permission scope is unchanged.
