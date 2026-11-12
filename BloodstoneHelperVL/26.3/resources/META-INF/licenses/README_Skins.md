# Skins assets: Rudy and Verity

The BloodHelper renderer/converter is original project code. Model assets retain their own conditions.

## Rudy

Source: user-supplied `pyz1k-client-1.21.11.jar`.
SHA-256: `ab637a8e0378baebfcc9d68d4d4e211e3504aa16d0728372757aa244253ee063`.
Archive metadata: Pyz1k Client 1.0-SNAPSHOT, author `Yz1kk (based Divine)`, license `All-Rights-Reserved`.
Imported data: `assets/pyz1k/models/chicken/mesh_*.bin`, `rudy_sc_1.png`, `bruce_col.png`.
Only geometry, bone pivots and textures were imported; no donor classes are included or executed.
Changes: uniform scaling to player height, BHS2 conversion, integration with vanilla player animation.
The original textures remain unmodified. This provenance record is not a new license or a grant of redistribution rights.

## Verity

This work is based on "Verity Full Model"
(https://sketchfab.com/3d-models/verity-full-model-696156ec15c248098b87e6fc32dd34e7)
by ChocoChip67 (https://sketchfab.com/ChocoChip67),
licensed under CC-BY-4.0 (https://creativecommons.org/licenses/by/4.0/).
The original license notice is included as VERITY-LICENSE.txt.

Source GLB SHA-256: `8de644c615b0fadf98869704ca393a0ed7b12dd78c69730f2cf2ce7508fa2d49`.
Changes: coordinate conversion and uniform scale to player height; original closed/open mouth states;
walk/walkback rotation and jump translation/scale tracks connected to game movement and combat events.
Sphere vertex normals are smoothed to make the open and closed expressions share the same lighting.
Unused expression states and animation clips are omitted. No original asset is relicensed under MIT.

Rebuild: `python tools/skins/generate_models.py` reads preserved source data in `tools/skins/source/`.
`tools/skins/render_previews.py` makes GUI thumbnails from the resulting geometry and textures (NumPy/Pillow).
