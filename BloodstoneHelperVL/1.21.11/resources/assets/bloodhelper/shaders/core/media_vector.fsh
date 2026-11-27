#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec2 iconPosition;
in vec4 vertexColor;
out vec4 fragColor;

// Playback proportions adapted from Lucide's play/pause/skip SVGs at
// 440a1e1dfb1e712505308da46bead3c28b1f1398. Filled geometry preserves
// legibility at HUD scale; see META-INF/licenses/LICENSE_Lucide.txt.
float segmentDistance(vec2 p, vec2 a, vec2 b) {
    vec2 ab = b - a;
    return length(p - a - ab * clamp(dot(p - a, ab) / dot(ab, ab), 0.0, 1.0));
}
float triangleDistance(vec2 p, vec2 a, vec2 b, vec2 c) {
    float d = min(segmentDistance(p, a, b), min(segmentDistance(p, b, c), segmentDistance(p, c, a)));
    vec2 ab = b-a, bc = c-b, ca = a-c;
    vec2 ap = p-a, bp = p-b, cp = p-c;
    float s1 = ab.x*ap.y - ab.y*ap.x;
    float s2 = bc.x*bp.y - bc.y*bp.x;
    float s3 = ca.x*cp.y - ca.y*cp.x;
    bool inside = (s1 >= 0.0 && s2 >= 0.0 && s3 >= 0.0) || (s1 <= 0.0 && s2 <= 0.0 && s3 <= 0.0);
    return inside ? -d : d;
}
float roundedBox(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + radius;
    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - radius;
}
void main() {
    vec2 p = iconPosition;
    float d;
#if ICON_ID == 0
    d = triangleDistance(p, vec2(7.0,5.0), vec2(19.0,12.0), vec2(7.0,19.0)) - 1.25;
#elif ICON_ID == 1
    d = min(roundedBox(p-vec2(7.5,12.0),vec2(2.5,9.0),1.0), roundedBox(p-vec2(16.5,12.0),vec2(2.5,9.0),1.0));
#elif ICON_ID == 2 || ICON_ID == 3
    #if ICON_ID == 3
        p.x = 24.0 - p.x;
    #endif
    d = min(triangleDistance(p,vec2(8.0,12.0),vec2(19.0,6.0),vec2(19.0,18.0))-1.25,
            segmentDistance(p,vec2(3.0,4.0),vec2(3.0,20.0))-1.0);
#elif ICON_ID == 4
    d = length(p-vec2(12.0))-10.0;
#elif ICON_ID == 6
    // Hotbar slots with a raised selected slot and selection tick.
    d = abs(roundedBox(p-vec2(12.0,15.0),vec2(9.5,4.5),1.2))-0.85;
    d = min(d, abs(roundedBox(p-vec2(12.0,14.0),vec2(3.2,5.5),0.7))-0.85);
    d = min(d, segmentDistance(p,vec2(12.0,4.0),vec2(12.0,6.0))-0.9);
#elif ICON_ID == 7
    // Keyboard: large key clusters remain readable at the 17 px card size.
    d = abs(roundedBox(p-vec2(12.0,12.0),vec2(9.5,7.0),1.5))-0.85;
    d = min(d, roundedBox(p-vec2(6.5,9.0),vec2(0.9),0.25));
    d = min(d, roundedBox(p-vec2(12.0,9.0),vec2(0.9),0.25));
    d = min(d, roundedBox(p-vec2(17.5,9.0),vec2(0.9),0.25));
    d = min(d, segmentDistance(p,vec2(7.0,15.0),vec2(17.0,15.0))-0.9);
#elif ICON_ID == 8
    // Totem silhouette: round head, outstretched arms and split feet.
    d = abs(length(p-vec2(12.0,6.0))-3.0)-0.9;
    d = min(d, abs(roundedBox(p-vec2(12.0,13.5),vec2(3.0,4.5),0.8))-0.9);
    d = min(d, segmentDistance(p,vec2(9.0,11.0),vec2(4.0,9.0))-0.9);
    d = min(d, segmentDistance(p,vec2(4.0,9.0),vec2(4.0,14.0))-0.9);
    d = min(d, segmentDistance(p,vec2(15.0,11.0),vec2(20.0,9.0))-0.9);
    d = min(d, segmentDistance(p,vec2(20.0,9.0),vec2(20.0,14.0))-0.9);
    d = min(d, segmentDistance(p,vec2(10.0,18.0),vec2(8.0,21.0))-0.9);
    d = min(d, segmentDistance(p,vec2(14.0,18.0),vec2(16.0,21.0))-0.9);
#elif ICON_ID == 9
    // Scoreboard with a header and two scored rows.
    d = abs(roundedBox(p-vec2(12.0,12.0),vec2(9.0),1.5))-0.85;
    d = min(d, segmentDistance(p,vec2(3.5,8.0),vec2(20.5,8.0))-0.85);
    d = min(d, segmentDistance(p,vec2(7.0,12.5),vec2(12.0,12.5))-0.85);
    d = min(d, segmentDistance(p,vec2(7.0,17.0),vec2(12.0,17.0))-0.85);
    d = min(d, length(p-vec2(17.0,12.5))-0.95);
    d = min(d, length(p-vec2(17.0,17.0))-0.95);
#elif ICON_ID == 10
    // Title typography, visually distinct from a multi-line action bar.
    d = segmentDistance(p,vec2(4.0,5.0),vec2(20.0,5.0))-1.0;
    d = min(d, segmentDistance(p,vec2(4.0,5.0),vec2(4.0,8.0))-1.0);
    d = min(d, segmentDistance(p,vec2(20.0,5.0),vec2(20.0,8.0))-1.0);
    d = min(d, segmentDistance(p,vec2(12.0,5.0),vec2(12.0,20.0))-1.0);
    d = min(d, segmentDistance(p,vec2(8.0,20.0),vec2(16.0,20.0))-1.0);
#elif ICON_ID == 11
    // Short status text above the bottom bar.
    d = abs(roundedBox(p-vec2(12.0,17.0),vec2(9.5,3.0),1.0))-0.85;
    d = min(d, segmentDistance(p,vec2(7.0,9.0),vec2(17.0,9.0))-0.9);
    d = min(d, segmentDistance(p,vec2(10.0,4.0),vec2(14.0,4.0))-0.9);
#elif ICON_ID == 12
    // Cloud contour and three separate rain drops.
    float cloud = min(length(p-vec2(9.5,8.0))-4.0, length(p-vec2(15.0,9.0))-3.5);
    cloud = min(cloud, roundedBox(p-vec2(12.0,12.0),vec2(8.5,3.0),2.8));
    d = abs(cloud)-0.85;
    d = min(d, segmentDistance(p,vec2(7.0,18.0),vec2(5.5,21.0))-0.85);
    d = min(d, segmentDistance(p,vec2(13.0,18.0),vec2(11.5,21.0))-0.85);
    d = min(d, segmentDistance(p,vec2(19.0,18.0),vec2(17.5,21.0))-0.85);
#elif ICON_ID == 13
    // Landscape and a small sun for biome ambience.
    d = segmentDistance(p,vec2(2.5,20.0),vec2(8.5,8.5))-0.9;
    d = min(d, segmentDistance(p,vec2(8.5,8.5),vec2(15.0,20.0))-0.9);
    d = min(d, segmentDistance(p,vec2(12.0,15.0),vec2(16.0,10.5))-0.9);
    d = min(d, segmentDistance(p,vec2(16.0,10.5),vec2(21.5,20.0))-0.9);
    d = min(d, segmentDistance(p,vec2(2.5,20.0),vec2(21.5,20.0))-0.9);
    d = min(d, abs(length(p-vec2(17.0,5.0))-2.0)-0.85);
#else
    d = max(abs(p.x-12.0)-10.0,abs(p.y-12.0)-10.0);
#endif
    float aa = max(fwidth(d), 0.001);
    float coverage = 1.0 - smoothstep(-aa*0.5, aa*0.5, d);
    vec4 color = vertexColor * ColorModulator;
    fragColor = vec4(color.rgb, color.a * coverage);
    if (fragColor.a <= 0.0) discard;
}
