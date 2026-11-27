#version 330
#extension GL_ARB_separate_shader_objects : require
uniform sampler2D CurrentFrame;
uniform sampler2D HistoryFrame;
layout(std140) uniform MotionBlur { vec4 MotionValues; };
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;
vec3 linearColor(vec3 color) {
    return mix(color / 12.92, pow((color + 0.055) / 1.055, vec3(2.4)), step(vec3(0.04045), color));
}
vec3 displayColor(vec3 color) {
    color = max(color, vec3(0.0));
    return mix(color * 12.92, 1.055 * pow(color, vec3(1.0 / 2.4)) - 0.055, step(vec3(0.0031308), color));
}
void main() {
    vec4 current = texture(CurrentFrame, texCoord);
    vec3 history = texture(HistoryFrame, texCoord).rgb;
    // Accumulate light, not encoded sRGB: moving highlights retain their brightness.
    fragColor = vec4(displayColor(mix(linearColor(current.rgb), linearColor(history), MotionValues.x)), current.a);
}
