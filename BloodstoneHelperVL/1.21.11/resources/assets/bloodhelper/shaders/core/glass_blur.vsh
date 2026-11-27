#version 330

out vec2 texCoord;

void main() {
    // Full-screen triangle; no vertex buffer or GUI projection state required.
    vec2 position = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
    texCoord = position;
    gl_Position = vec4(position * 2.0 - 1.0, 0.0, 1.0);
}
