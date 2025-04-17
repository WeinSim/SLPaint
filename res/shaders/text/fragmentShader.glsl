#version 400 core

in vec2 position;
in vec2 textureCoords;
in vec3 color;
in vec2 boundingBoxMin;
in vec2 boundingBoxMax;

out vec4 outColor;

uniform sampler2D textureSamplers[4];

void main(void) {

    if (position.x < boundingBoxMin.x) {
        discard;
    }
    if (position.x > boundingBoxMax.x) {
        discard;
    }
    if (position.y < boundingBoxMin.y) {
        discard;
    }
    if (position.y > boundingBoxMax.y) {
        discard;
    }

    int page = int(floor(textureCoords.x));
    vec2 actualTextureCoords = vec2(textureCoords.x - page, textureCoords.y);

    vec4 textureColor = texture(textureSamplers[page], actualTextureCoords);
    float alpha = textureColor.r;

    outColor = vec4(color, alpha);
}