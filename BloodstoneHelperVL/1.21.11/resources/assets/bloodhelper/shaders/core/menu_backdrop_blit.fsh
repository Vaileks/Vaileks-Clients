#version 330

uniform sampler2D Sampler0;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    // The Gaussian filters are complete. Only a bilinear reconstruction remains
    // at full viewport resolution; do not run another wide kernel here.
    fragColor = textureLod(Sampler0, texCoord, 0.0);
}
