#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out vec2 uvCoords;

uniform mat3 viewMatrix;
uniform mat3 uiMatrix;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

void main(void) {
    for (int i = 0; i < 4; i++) {
        vec2 offset = cornerOffsets[i];
        vec2 swOffset = offset;
        vec3 screenPos = viewMatrix * uiMatrix * vec3(offset, 1.0);
        gl_Position = vec4(screenPos.xy, 0, 1);

        uvCoords = offset;
        EmitVertex();
    }
    EndPrimitive();
}