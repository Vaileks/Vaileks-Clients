# Zoomify adaptation

The BLOODHELPER Zoom state and FOV-return integration adapt Zoomify by isXander,
tag `2.15.2+1.21.11`, commit `f5f923279401eac8bbb6b7ac406c869f7377c7b2`.
Upstream declares `LGPL-3.0-or-later` in its `build.gradle.kts`.
The original LGPL and accompanying GPL text are included here unchanged.

The adapted source files retain LGPL-3.0-or-later notices. Original source
snapshots are retained in the development checkout at `docs/licenses/zoomify-upstream/`;
exact paths, changes and hashes are recorded in `zoomify-provenance.json`.
The runtime JAR does not bundle source files. Zoomify's settings UI, Kotlin runtime and other
features are not copied into this adaptation.

This file accompanies the runtime JAR, not a source distribution. The development
checkout contains the adapted Java files, application integration, build scripts,
Gradle wrapper and resources. Minecraft 1.21.x variants target Java 21; Minecraft
26.x variants target Java 25. Each variant is built from its own exact-version
project. No special signature or installation restriction is added: a rebuilt
JAR can replace the installed BLOODHELPER JAR in the corresponding Fabric profile.
Modification of the LGPL-covered parts, relinking and reverse engineering to
debug modifications are permitted under their license. Distribution of this
adaptation remains subject to the corresponding-source requirements of LGPLv3;
this notice does not assert that a source archive accompanies this runtime JAR.
Other bundled assets retain their separately recorded licenses; this notice does
not relicense them.

On 2026-10-14 the renderer adaptation was corrected to include Zoomify's default
affectHandFov behavior and its 1.21.11 bobView injection. Both starting zoom and
wheel adjustment allow 100x. Disabling the module still preserves incoming FOV
and mouse input. Source configuration confirms affectHandFov defaults to true:
https://github.com/isXander/Zoomify/blob/f5f923279401eac8bbb6b7ac406c869f7377c7b2/src/main/kotlin/dev/isxander/zoomify/config/ZoomifySettings.kt

On 2026-10-15, the initial interpolation was replaced with the pinned upstream
TransitionInterpolator/TransitionType exponential curves and inverse direction
mapping. The new ZoomInterpolation.java remains LGPL-3.0-or-later. Exponential
endpoints use floating-point normalization to avoid the upstream integer-division
discontinuity. Independent geometric scroll smoothing uses the continuous-time
equivalent of the upstream 70% smoothing default, so rendering frequency does not
change its speed. Current scroll is folded into the release transition without
jumping when initial zoom is still incomplete.

BLOODHELPER's default initial magnification is 2.5x. Timings match upstream's
1.0-second in / 0.5-second out defaults. The GUI animation preference no longer
controls camera smoothing; explicit zero transition duration still means instant.
A one-time migration recognizes the complete previous 4x / 0.20 / 0.15 default
bundle. Distinct custom configurations remain unchanged.
