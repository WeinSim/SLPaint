#version 400 core

in vec2 passUVCoords;

out vec4 outColor;

uniform sampler2D textureSampler;

void main(void) {

    vec4 sampleColor = texture(textureSampler, passUVCoords);
    // outColor = vec4(sampleColor.xyz, 0.5);
    outColor = sampleColor;
}