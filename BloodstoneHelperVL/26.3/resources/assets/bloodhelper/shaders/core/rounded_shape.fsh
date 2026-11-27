#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
uniform sampler2D Sampler0;
layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPosition;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in float radius;
layout(location = 4) flat in float outline;
layout(location = 0) out vec4 fragColor;
void main() {
    vec2 q = abs(localPosition) - halfSize + radius;
    float distance = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - radius;
    float aa = max(fwidth(distance), 0.001);
    float coverage = 1.0 - smoothstep(-aa * 0.5, aa * 0.5, distance);
    if (outline > 0.5) coverage *= smoothstep(-1.0 - aa * 0.5, -1.0 + aa * 0.5, distance);
    if (coverage <= 0.0) discard;
    vec4 tint = vertexColor * ColorModulator * texture(Sampler0, vec2(0.5));
    fragColor = vec4(tint.rgb, tint.a * coverage);
}
