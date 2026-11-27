#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec2 UV0;
in vec4 Color;
out vec4 vertexColor;
out vec2 localPosition;
flat out vec2 waveSize;
flat out vec2 wavePhase;
flat out float waveEnergy;
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
