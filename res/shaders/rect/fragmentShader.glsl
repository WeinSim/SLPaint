#version 400 core

in vec4 color;
in vec4 color2;
in float applyCheckerboard;
in vec2 relativePos;

out vec4 outColor;

uniform float checkerboardSize;
uniform vec2 size;
uniform float strokeWeight;

void main(void) {

    if (applyCheckerboard < 0.5) {
        outColor = color;
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
        outColor = (xComp + yComp) % 2 == 0 ? color : color2;
    }
}