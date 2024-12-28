#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 14) out;
// layout (line_strip, max_vertices = 5) out;

out vec4 color;
out vec4 color2;
out float applyCheckerboard;
out float distAlongEdge;
out vec2 relativePos;

uniform vec2 position;
uniform vec2 size;

uniform vec3 fill;
uniform int doFill;
uniform vec3 stroke;
uniform int doStroke;
uniform float strokeWeight;
uniform int doCheckerboardStroke;
uniform vec3 checkerboardStroke;

uniform mat3 viewMatrix;
uniform mat3 uiMatrix;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

void main(void) {
    applyCheckerboard = 0;
    if (doFill > 0.5) {
        for (int i = 0; i < 4; i++) {
            vec3 screenPos = viewMatrix * uiMatrix * vec3((position + cornerOffsets[i] * size), 1.0);
            color = vec4(fill, 1.0);
            gl_Position = vec4(screenPos.xy, 0, 1);
            EmitVertex();
        }
        EndPrimitive();
    }
    if (doStroke > 0.5) {
        applyCheckerboard = doCheckerboardStroke;
        color2 = vec4(checkerboardStroke, 1.0);

        float[5] distsAlongEdge = float[5](
            0,
            size.y,
            size.y + size.x,
            size.x,
            0
        );

        for (int i = 0; i < 5; i++) {
            int offsetIndex = i % 4;
            if (offsetIndex >= 2) {
                offsetIndex = 5 - offsetIndex;
            }
            vec2 offset = cornerOffsets[offsetIndex];
            vec2 basePos = position + offset * size;
            color = vec4(stroke, 1.0);

            vec2 swOffset = (offset - 0.5) * strokeWeight;

            // add this line for inset outlines
            // basePos -= swOffset;

            distAlongEdge = distsAlongEdge[i];
            vec3 screenPos = vec3(basePos +  swOffset, 1.0);
            screenPos = viewMatrix * uiMatrix * screenPos;
            gl_Position = vec4(screenPos.xy, 0, 1);
            relativePos = offset * size + swOffset;
            EmitVertex();

            screenPos = vec3(basePos - swOffset, 1.0);
            screenPos = viewMatrix * uiMatrix * screenPos;
            gl_Position = vec4(screenPos.xy, 0, 1);
            relativePos = offset * size - swOffset;
            EmitVertex();
        }
        EndPrimitive();
    }
}