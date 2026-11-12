#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
uniform sampler2D Sampler0;
layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPosition;
layout(location = 2) flat in vec2 waveSize;
layout(location = 3) flat in vec2 wavePhase;
layout(location = 4) flat in float waveEnergy;
layout(location = 0) out vec4 fragColor;
float waveY(float t) {
    float envelope=pow(max(sin(3.14159265359*t),0.0),.65);
    float wave=sin(t*15.0796447372-wavePhase.x)*.74+sin(t*22.6194671058+wavePhase.y)*.26;
    return waveSize.y*.5+wave*envelope*waveSize.y*.43*waveEnergy;
}
void main() {
    float t=clamp(localPosition.x/waveSize.x,0.0,1.0);
    float delta=.005;
    float a=max(0.0,t-delta),b=min(1.0,t+delta);
    float slope=(waveY(b)-waveY(a))/max((b-a)*waveSize.x,.001);
    float dy=localPosition.y-waveY(t);
    float distance=abs(dy)/sqrt(1.0+slope*slope);
    if(localPosition.x<0.0||localPosition.x>waveSize.x)
        distance=length(vec2(localPosition.x-t*waveSize.x,dy));
    float aa=max(fwidth(distance),.25);
    float coverage=1.0-smoothstep(.72-aa*.5,.72+aa*.5,distance);
    if(coverage<=0.0)discard;
    vec4 tint=vertexColor*ColorModulator*texture(Sampler0,vec2(.5));
    fragColor=vec4(tint.rgb,tint.a*coverage);
}
