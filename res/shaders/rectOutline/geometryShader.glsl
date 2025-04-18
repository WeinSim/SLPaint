#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 10) out;

layout(std140)
uniform RectData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
    // (r, g, b, size). if size == -1, then no checkerboard at all
    vec4[256] checkerboard;
} rectData;

uniform mat3 viewMatrix;

in vec3[] pass_position;
in vec2[] pass_size;
in vec4[] pass_color1SW;
in int[] pass_dataIndex;

out vec2 relativePos;
out vec2 size;
out float strokeWeight;
out vec3 color1;
out vec3 color2;
out float checkerboardSize;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;

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
    vec3 position = pass_position[0];
    vec2 size = pass_size[0];

    int dataIndex = pass_dataIndex[0];
    relativeBoundingBoxMin = rectData.boundingBox[dataIndex].xy - position.xy;
    relativeBoundingBoxMax = rectData.boundingBox[dataIndex].zw - position.xy;

    // first check if entire rect is outside of the bounding box
    if (relativeBoundingBoxMax.x  < 0) {
        return;
    }
    if (relativeBoundingBoxMax.y < 0) {
        return;
    }
    if (relativeBoundingBoxMin.x > size.x) {
        return;
    }
    if (relativeBoundingBoxMin.y > size.y) {
        return;
    }

    strokeWeight = pass_color1SW[0].a;
    color1 = pass_color1SW[0].rgb;
    color2 = rectData.checkerboard[dataIndex].rgb;
    checkerboardSize = rectData.checkerboard[dataIndex].a;

    for (int i = 0; i < 5; i++) {
        int offsetIndex = i % 4;
        if (offsetIndex >= 2) {
            offsetIndex = 5 - offsetIndex;
        }
        vec2 offset = cornerOffsets[offsetIndex];
        vec2 basePos = position.xy + offset * size;

        vec2 swOffset = (offset - 0.5) * strokeWeight;

        // add this line for inset outlines
        // basePos -= swOffset;

        vec3 screenPos = vec3(basePos + swOffset, 1.0);
        screenPos = viewMatrix * vecToInt(screenPos);
        gl_Position = vec4(screenPos.xy, position.z, 1.0);
        relativePos = offset * size + swOffset;
        EmitVertex();

        screenPos = vec3(basePos - swOffset, 1.0);
        screenPos = viewMatrix * vecToInt(screenPos);
        gl_Position = vec4(screenPos.xy, position.z, 1.0);
        relativePos = offset * size - swOffset;
        EmitVertex();
    }

    EndPrimitive();
}