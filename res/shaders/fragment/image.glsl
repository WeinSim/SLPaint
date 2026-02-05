#version 400 core

in vec2 relativePos;
in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec2 uvCoords;
flat in int samplerID;

out vec4 outColor;

uniform sampler2D textureSamplers[32];

void main(void) {

    if (relativePos.x < relativeBoundingBoxMin.x) {
        discard;
    }
    if (relativePos.y < relativeBoundingBoxMin.y) {
        discard;
    }
    if (relativePos.x > relativeBoundingBoxMax.x) {
        discard;
    }
    if (relativePos.y > relativeBoundingBoxMax.y) {
        discard;
    }

    vec4 sampleColor = texture(textureSamplers[samplerID], uvCoords);
    // sampleColor = texture(textureSamplers[0], uvCoords);

    outColor = sampleColor;
    // outColor.a = 1;
    // outColor = vec4(0.5 * (1 + samplerID), 0.5, 0.5, 1);
    // outColor.xy = uvCoords;
}