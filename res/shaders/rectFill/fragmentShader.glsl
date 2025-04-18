#version 400 core

in vec2 relativePos;
in vec4 color1;
in vec3 color2;
in float checkerboardSize;
in vec2 relativeBoundingBoxMin;
in vec2 relativeBoundingBoxMax;

out vec4 outColor;

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

    if (checkerboardSize < 0) {
        outColor = color1;
    } else {
        int xComp = int(relativePos.x / checkerboardSize);
        int yComp = int(relativePos.y / checkerboardSize);
        outColor = vec4((xComp + yComp) % 2 == 0 ? color1.rgb : color2.rgb, color1.a);
    }
}