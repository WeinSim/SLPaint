#version 400 core

in vec2 textureCoords;

out vec4 outColor;

uniform vec3 fill;
uniform int doFill;
uniform sampler2D textureSampler;

void main(void) {
    vec4 textureColor = texture(textureSampler, textureCoords);
    outColor = vec4(fill, textureColor.r * doFill);
}