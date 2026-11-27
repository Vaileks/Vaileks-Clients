#version 330
#extension GL_ARB_separate_shader_objects : require
layout(location = 0) in vec2 shapePosition;
layout(location = 1) in vec4 vertexColor;
layout(location = 0) out vec4 fragColor;
#ifdef TEXT
uniform sampler2D Sampler0;
#endif
void main() {
#ifdef TEXT
    vec4 glyph = texture(Sampler0, shapePosition);
    fragColor = glyph * vertexColor;
    if (fragColor.a < 0.005) discard;
#else
#ifdef RING
    float radius = length(shapePosition);
    float edge = abs(radius - 0.94);
    float aa = max(fwidth(radius), 0.0001);
    float core = 1.0 - smoothstep(aa * 0.6, aa * 1.8, edge);
    float glow = 0.22 * exp(-edge / max(aa * 4.0, 0.009));
    float fill = 0.035 * (1.0 - smoothstep(0.90, 0.94, radius));
    float coverage = max(core, glow) + fill;
#else
    float edge = abs(shapePosition.y);
    float aa = max(fwidth(edge), 0.001);
    float core = 1.0 - smoothstep(0.24 - aa * 0.5, 0.24 + aa * 0.5, edge);
    float glow = 0.24 * exp(-edge * edge * 6.0);
    float coverage = max(core, glow) * (1.0 - smoothstep(0.85, 1.0, edge));
#endif
    if (coverage < 0.005) discard;
    fragColor = vec4(vertexColor.rgb, vertexColor.a * min(coverage, 1.0));
#endif
}
