#version 400 core

in vec2 relativePos;
in vec2 uvCoords;
in vec4 color;
in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;

out vec4 outColor;

void main(void) {
    float mag = uvCoords.x * uvCoords.x + uvCoords.y * uvCoords.y;
    if (mag > 1) {
        discard;
    }

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

    outColor = color;
}