#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

layout(std140)
uniform RectFillData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
    // (r, g, b, size). if size == -1, then no checkerboard at all
    vec4[256] checkerboard;
} rectFillData;

uniform mat3 viewMatrix;

in vec3[] pass_position;
in vec2[] pass_size;
in vec4[] pass_color1;
in int[] pass_dataIndex;

out vec2 relativePos;
out vec4 color1;
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
    relativeBoundingBoxMin = rectFillData.boundingBox[dataIndex].xy - position.xy;
    relativeBoundingBoxMax = rectFillData.boundingBox[dataIndex].zw - position.xy;

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

    color1 = pass_color1[0];
    color2 = rectFillData.checkerboard[dataIndex].rgb;
    checkerboardSize = rectFillData.checkerboard[dataIndex].a;

    for (int i = 0; i < 4; i++) {
        relativePos = cornerOffsets[i] * size;
        vec3 screenPos = viewMatrix * vecToInt(vec3(position.xy + cornerOffsets[i] * size, 1.0));
        gl_Position = vec4(screenPos.xy, position.z, 1.0);
        EmitVertex();
    }
    EndPrimitive();
}