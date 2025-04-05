#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out vec2 passUVCoords;

uniform mat3 viewMatrix;
uniform mat3 transformationMatrix;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

void main(void) {
    for (int i = 0; i < 4; i++) {
        vec3 screenPos = transformationMatrix * vec3(cornerOffsets[i], 1.0);
        passUVCoords = cornerOffsets[i];
        screenPos = viewMatrix * vec3(screenPos.xy, 1.0);
        gl_Position = vec4(screenPos.xy, 0, 1);
        EmitVertex();
    }
    EndPrimitive();
}