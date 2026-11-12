#version 330

// Separable Gaussian kernel adapted from ybouane/liquidglass (MIT),
// src/shaders.ts @ 59af227103795f06421a96e2c7d3dceb081decd2, FS_BLUR.
// See the bundled Liquid Glass third-party notice.

uniform sampler2D Sampler0;
layout(std140) uniform GlassBlur { vec4 BlurStep; };
in vec2 texCoord;
out vec4 fragColor;

vec3 linearColor(vec3 color) {
    return mix(color / 12.92, pow((color + 0.055) / 1.055, vec3(2.4)), step(vec3(0.04045), color));
}
vec3 displayColor(vec3 color) {
    color = max(color, vec3(0.0));
    return mix(color * 12.92, 1.055 * pow(color, vec3(1.0 / 2.4)) - 0.055, step(vec3(0.0031308), color));
}
vec3 sampleLinear(vec2 position) {
    return linearColor(textureLod(Sampler0, position, 0.0).rgb);
}

void main() {
#ifdef HORIZONTAL
    vec2 direction = vec2(BlurStep.x, 0.0);
#else
    vec2 direction = vec2(0.0, BlurStep.y);
#endif
#ifdef BLUR_LOW
    // Five wider taps keep a smooth, symmetric kernel at quarter resolution.
    vec3 color = sampleLinear(texCoord) * 0.294118;
    color += (sampleLinear(texCoord + direction * 1.5) + sampleLinear(texCoord - direction * 1.5)) * 0.235294;
    color += (sampleLinear(texCoord + direction * 3.5) + sampleLinear(texCoord - direction * 3.5)) * 0.117647;
#else
    vec3 color = sampleLinear(texCoord) * 0.227027;
    color += (sampleLinear(texCoord + direction) + sampleLinear(texCoord - direction)) * 0.194594;
    color += (sampleLinear(texCoord + direction * 2.0) + sampleLinear(texCoord - direction * 2.0)) * 0.121622;
    color += (sampleLinear(texCoord + direction * 3.0) + sampleLinear(texCoord - direction * 3.0)) * 0.054054;
    color += (sampleLinear(texCoord + direction * 4.0) + sampleLinear(texCoord - direction * 4.0)) * 0.016216;
#endif
    // Scene targets contain display-referred RGBA8. Encode each intermediate pass
    // so 8-bit dark gradients retain precision; optical filtering remains linear.
    fragColor = vec4(displayColor(color), 1.0);
}
