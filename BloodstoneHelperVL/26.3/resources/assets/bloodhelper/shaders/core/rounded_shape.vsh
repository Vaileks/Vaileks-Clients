#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPosition;
layout(location = 2) flat out vec2 halfSize;
layout(location = 3) flat out float radius;
layout(location = 4) flat out float outline;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    vec2 metadata = abs(UV0);
    halfSize = mod(metadata, 4096.0) * 0.5;
    radius = floor(metadata.x / 4096.0);
    outline = floor(metadata.y / 4096.0);
    localPosition = sign(UV0) * halfSize;
}
