#version 400 core

in vec2 relativePos;

out vec4 outColor;

uniform vec3 color1;
uniform vec3 color2;
uniform float fillAlpha;
uniform int applyCheckerboard;
uniform float checkerboardSize;

void main(void) {
    if (applyCheckerboard == 0) {
        outColor = vec4(color1, fillAlpha);
    } else {
        int xComp = int(relativePos.x / checkerboardSize);
        int yComp = int(relativePos.y / checkerboardSize);
        outColor = vec4((xComp + yComp) % 2 == 0 ? color1 : color2, fillAlpha);
    }
}