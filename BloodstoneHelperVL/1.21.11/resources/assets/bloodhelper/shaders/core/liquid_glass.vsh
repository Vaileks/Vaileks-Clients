#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec4 vertexColor;
out vec2 localPosition;
flat out vec4 shape;
flat out float refraction;
flat out float menuMaterial;
flat out float materialDensity;
flat out float readability;

flat out float reglassProfile;

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
