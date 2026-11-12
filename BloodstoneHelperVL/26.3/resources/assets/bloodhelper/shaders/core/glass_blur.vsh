#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) out vec2 texCoord;

void main() {
    // Full-screen triangle; no vertex buffer or GUI projection state required.
    vec2 position = vec2(float((gl_VertexIndex << 1) & 2), float(gl_VertexIndex & 2));
    texCoord = position;
    gl_Position = vec4(position * 2.0 - 1.0, 0.0, 1.0);
}
