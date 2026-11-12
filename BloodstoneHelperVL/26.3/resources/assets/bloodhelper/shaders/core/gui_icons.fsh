#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

void main() {
    float distance = texture(Sampler0, texCoord0).r - 0.5;
    // The generator stores a total distance range of 16 atlas texels. Derivatives
    // keep edge coverage one screen pixel wide, including fractional menu scales.
    vec2 unitRange = vec2(16.0) / vec2(textureSize(Sampler0, 0));
    vec2 screenTexSize = 1.0 / max(fwidth(texCoord0), vec2(0.000001));
    float screenRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);
    float coverage = clamp(distance * screenRange + 0.5, 0.0, 1.0);
    vec4 color = vertexColor * ColorModulator;
    fragColor = vec4(color.rgb, color.a * coverage);
    if (fragColor.a <= 0.0) discard;
}
