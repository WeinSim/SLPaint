#version 400 core

in vec2 relativePos;

out vec4 outColor;

uniform vec3 color1;
uniform vec3 color2;
uniform vec2 size;
uniform float strokeWeight;
uniform int applyCheckerboard;
uniform float checkerboardSize;

void main(void) {
    if (applyCheckerboard == 0) {
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