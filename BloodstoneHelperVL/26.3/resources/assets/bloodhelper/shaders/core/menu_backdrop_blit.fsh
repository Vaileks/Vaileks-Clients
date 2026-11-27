#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    // The Gaussian filters are complete. Only a bilinear reconstruction remains
    // at full viewport resolution; do not run another wide kernel here.
    fragColor = textureLod(Sampler0, texCoord, 0.0);
}
