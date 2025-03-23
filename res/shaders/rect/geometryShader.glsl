#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 14) out;
// layout (line_strip, max_vertices = 5) out;

out vec4 color;
out vec4 color2;
out float applyCheckerboard;
out vec2 relativePos;

uniform vec2 position;
uniform vec2 size;

uniform vec3 fill;
uniform float fillAlpha;
uniform int fillMode;
uniform vec3 stroke;
uniform int strokeMode;
uniform float strokeWeight;
uniform vec3 checkerboardColor1;
uniform vec3 checkerboardColor2;

uniform mat3 viewMatrix;
uniform mat3 uiMatrix;
uniform float depth;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

void main(void) {
    if (fillMode > 0) {
        if (fillMode == 1) {
            // normal fill
            applyCheckerboard = 0;
            color = vec4(fill, fillAlpha);
        } else {
            // checkerboard fill
            applyCheckerboard = 1;
            color = vec4(checkerboardColor1, fillAlpha);
            color2 = vec4(checkerboardColor2, fillAlpha);
        }

        for (int i = 0; i < 4; i++) {
            relativePos = cornerOffsets[i] * size;
            vec3 screenPos = viewMatrix * uiMatrix * vec3(position + cornerOffsets[i] * size, 1.0);
            gl_Position = vec4(screenPos.xy, depth, 1.0);
            EmitVertex();
        }
        EndPrimitive();
    }
    if (strokeMode > 0) {
        if (strokeMode == 1) {
            // normal stroke
            applyCheckerboard = 0;
            color = vec4(stroke, 1.0);
        } else {
            // checkerboard stroke
            applyCheckerboard = 1;
            color = vec4(checkerboardColor1, 1.0);
            color2 = vec4(checkerboardColor2, 1.0);
        }

        // float[5] distsAlongEdge = float[5](
        //     0,
        //     size.y,
        //     size.y + size.x,
        //     size.x,
        //     0
        // );

        for (int i = 0; i < 5; i++) {
            int offsetIndex = i % 4;
            if (offsetIndex >= 2) {
                offsetIndex = 5 - offsetIndex;
            }
            vec2 offset = cornerOffsets[offsetIndex];
            vec2 basePos = position + offset * size;

            vec2 swOffset = (offset - 0.5) * strokeWeight;

            // add this line for inset outlines
            // basePos -= swOffset;

            // distAlongEdge = distsAlongEdge[i];
            vec3 screenPos = vec3(basePos + swOffset, 1.0);
            screenPos = viewMatrix * uiMatrix * screenPos;
            gl_Position = vec4(screenPos.xy, depth, 1.0);
            relativePos = offset * size + swOffset;
            EmitVertex();

            screenPos = vec3(basePos - swOffset, 1.0);
            screenPos = viewMatrix * uiMatrix * screenPos;
            gl_Position = vec4(screenPos.xy, depth, 1.0);
            relativePos = offset * size - swOffset;
            EmitVertex();
        }
        EndPrimitive();
    }
}