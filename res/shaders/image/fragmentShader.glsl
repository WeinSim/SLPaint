#version 400 core

in vec2 relativePos;
in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;
in vec2 uvCoords;
flat in int samplerID;

out vec4 outColor;

uniform sampler2D textureSamplers[16];

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

    // float alpha = sampleColor.a;
    // outColor = vec4(alpha, alpha, alpha, 1.0);

    outColor = sampleColor;
}