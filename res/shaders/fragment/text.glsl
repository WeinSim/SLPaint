#version 400 core

in vec2 relativePos;
in vec2 textureCoords;
in vec4 color;
in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;

out vec4 outColor;

uniform sampler2D textureSamplers[4];

void main(void) {

    if (relativePos.x < relativeBoundingBoxMin.x) {
        discard;
    }
    if (relativePos.x > relativeBoundingBoxMax.x) {
        discard;
    }
    if (relativePos.y < relativeBoundingBoxMin.y) {
        discard;
    }
    if (relativePos.y > relativeBoundingBoxMax.y) {
        discard;
    }

    int page = int(floor(textureCoords.x));
    vec2 actualTextureCoords = vec2(textureCoords.x - page, textureCoords.y);

    vec4 textureColor = texture(textureSamplers[page], actualTextureCoords);
    float alpha = color.a * textureColor.r;

    outColor = vec4(color.xyz, alpha);
}