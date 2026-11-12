#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPosition;
layout(location = 2) flat out vec2 waveSize;
layout(location = 3) flat out vec2 wavePhase;
layout(location = 4) flat out float waveEnergy;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor=Color;
    vec2 metadata=abs(UV0);
    vec2 size=mod(metadata,256.0);
    localPosition=(sign(UV0)*.5+.5)*size-2.0;
    waveSize=size-4.0;
    wavePhase=mod(floor(metadata/256.0),1024.0)*(6.28318530718/1024.0);
    waveEnergy=floor(metadata.y/262144.0)/63.0;
}
