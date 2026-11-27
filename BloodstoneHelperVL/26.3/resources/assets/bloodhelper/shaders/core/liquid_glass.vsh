#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPosition;
layout(location = 2) flat out vec4 shape;
layout(location = 3) flat out float refraction;
layout(location = 4) flat out float menuMaterial;
layout(location = 5) flat out float materialDensity;
layout(location = 6) flat out float readability;

layout(location = 7) flat out float reglassProfile;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    vec2 metadata = abs(UV0);
    vec2 dimensions = mod(metadata, 4096.0);
    float surface = floor(metadata.x / 4096.0);
    float corner = mod(surface, 64.0);
    menuMaterial = mod(floor(surface / 64.0), 2.0);
    materialDensity = floor(surface / 128.0) / 31.0;
    float effects = floor(metadata.y / 4096.0);
    shape = vec4(dimensions * 0.5, corner, mod(effects, 32.0));
    float bending = mod(floor(effects / 32.0), 32.0);
    reglassProfile = step(20.5, bending);
    refraction = reglassProfile > 0.5 ? bending - 21.0 : bending * 0.5;
    readability = floor(effects / 1024.0);
    localPosition = sign(UV0) * dimensions * 0.5;
}
