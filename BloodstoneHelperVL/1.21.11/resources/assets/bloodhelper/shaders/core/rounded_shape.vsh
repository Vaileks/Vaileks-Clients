#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec2 UV0;
in vec4 Color;
out vec4 vertexColor;
out vec2 localPosition;
flat out vec2 halfSize;
flat out float radius;
flat out float outline;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    vec2 metadata = abs(UV0);
    halfSize = mod(metadata, 4096.0) * 0.5;
    radius = floor(metadata.x / 4096.0);
    outline = floor(metadata.y / 4096.0);
    localPosition = sign(UV0) * halfSize;
}
