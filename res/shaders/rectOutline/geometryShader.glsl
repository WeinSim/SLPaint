#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 10) out;

out vec2 relativePos;

uniform vec2 position;
uniform vec2 size;

uniform float strokeWeight;

uniform mat3 viewMatrix;
uniform mat3 uiMatrix;
uniform float depth;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

/* Before screen space coordinates ([0, 1920] x [0, 1080]) are converted
 * to OpenGL coordinates ([-1, 1] x [-1, 1]), they are rounded to integer
 * values. This is to avoid inconsistent stroke weights of rectangles (a
 * stroke weight of 1px could appear as 0, 1 or 2 pixels without
 * rounding).
 */
vec3 vecToInt(vec3 v) {
    return vec3(floor(v.x), floor(v.y), floor(v.z));
}

void main(void) {
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

        vec3 screenPos = vec3(basePos + swOffset, 1.0);
        screenPos = viewMatrix * vecToInt(uiMatrix * screenPos);
        gl_Position = vec4(screenPos.xy, depth, 1.0);
        relativePos = offset * size + swOffset;
        EmitVertex();

        screenPos = vec3(basePos - swOffset, 1.0);
        screenPos = viewMatrix * vecToInt(uiMatrix * screenPos);
        gl_Position = vec4(screenPos.xy, depth, 1.0);
        relativePos = offset * size - swOffset;
        EmitVertex();
    }

    EndPrimitive();
}