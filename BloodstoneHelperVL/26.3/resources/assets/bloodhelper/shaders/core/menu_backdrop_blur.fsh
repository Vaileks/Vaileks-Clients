#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;
// xy: desired sigma in viewport UV; zw: one reduced-target texel in viewport UV.
layout(std140) uniform GlassBlur { vec4 BlurStep; };
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

vec3 linearColor(vec3 color) {
    return mix(color / 12.92, pow((color + 0.055) / 1.055, vec3(2.4)), step(vec3(0.04045), color));
}
vec3 displayColor(vec3 color) {
    color = max(color, vec3(0.0));
    return mix(color * 12.92, 1.055 * pow(color, vec3(1.0 / 2.4)) - 0.055, step(vec3(0.0031308), color));
}

vec3 filteredSource(vec2 position) {
#ifdef HORIZONTAL
    // A quarter-resolution destination covers 4x4 source pixels. Four bilinear
    // samples integrate that footprint before it is reduced, preserving thin
    // HUD strokes instead of aliasing them into a repeating grid.
    if (BlurStep.z * float(textureSize(Sampler0, 0).x) > 2.1) {
        vec2 halfFootprint = BlurStep.zw * 0.25;
        vec3 a = linearColor(textureLod(Sampler0, position + vec2(-halfFootprint.x, -halfFootprint.y), 0.0).rgb);
        vec3 b = linearColor(textureLod(Sampler0, position + vec2( halfFootprint.x, -halfFootprint.y), 0.0).rgb);
        vec3 c = linearColor(textureLod(Sampler0, position + vec2(-halfFootprint.x,  halfFootprint.y), 0.0).rgb);
        vec3 d = linearColor(textureLod(Sampler0, position + vec2( halfFootprint.x,  halfFootprint.y), 0.0).rgb);
        return (a + b + c + d) * 0.25;
    }
#endif
    // At half resolution the hardware bilinear footprint already covers 2x2.
    return linearColor(textureLod(Sampler0, position, 0.0).rgb);
}

void main() {
#ifdef HORIZONTAL
    vec2 direction = vec2(BlurStep.z, 0.0);
    float sigma = max(0.55, BlurStep.x / BlurStep.z);
#else
    vec2 direction = vec2(0.0, BlurStep.w);
    float sigma = max(0.55, BlurStep.y / BlurStep.w);
#endif
#ifdef BLUR_LOW
    const int MAX_RADIUS = 6;
#else
    const int MAX_RADIUS = 12;
#endif
    int radius = min(MAX_RADIUS, int(ceil(sigma * 3.0)));
    vec3 color = filteredSource(texCoord);
    float total = 1.0;
    // Adjacent reduced texels always touch. Changing sigma changes their
    // weights, rather than spreading a few samples into visible text copies.
    for (int i = 1; i <= MAX_RADIUS; ++i) {
        if (i > radius) break;
        float distance = float(i) / sigma;
        float weight = exp(-0.5 * distance * distance);
        vec2 offset = direction * float(i);
        color += (filteredSource(texCoord + offset) + filteredSource(texCoord - offset)) * weight;
        total += 2.0 * weight;
    }
    fragColor = vec4(displayColor(color / total), 1.0);
}
