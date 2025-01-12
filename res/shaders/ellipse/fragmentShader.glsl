#version 400 core

in vec2 uvCoords;

out vec4 outColor;

uniform vec3 fill;

void main(void) {
    float mag = uvCoords.x * uvCoords.x + uvCoords.y * uvCoords.y;
    if (mag > 1) {
        discard;
    }
    outColor = vec4(fill, 1.0);
}