#version 400 core

in vec2 relativePos;
in vec2 size;
in float strokeWeight;
in vec3 color1;
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
        outColor = vec4(color1, 1.0);
    } else {
        float width = size.x - strokeWeight / 2;
        float height = size.y - strokeWeight / 2;
        vec2 relPosClip = relativePos;
        if (relPosClip.x > width) {
            relPosClip.x -= width;
        }
        if (relPosClip.y > height) {
            relPosClip.y -= height;
        }
        int xComp = int(relPosClip.x / checkerboardSize);
        int yComp = int(relPosClip.y / checkerboardSize);
        outColor = vec4((xComp + yComp) % 2 == 0 ? color1 : color2, 1.0);
    }
}