#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

layout(std140)
uniform RectData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
    vec4[256] color2;
    // (size, 0, 0, 0). if size == -1, then no checkerboard at all
    vec4[256] checkerboardSize;
} rectData;

uniform mat3 viewMatrix;

in int[] pass_dataIndex;
in mat3[] pass_transformationMatrix;
in vec2[] pass_position;
in float[] pass_depth;
in vec2[] pass_size;
in vec4[] pass_color1;

out vec2 relativePos;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;
out vec4 color1;
out vec4 color2;
out float checkerboardSize;

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
    return vec3(floor(v.x), floor(v.y), v.z);
}

void main(void) {

    vec3 position = pass_transformationMatrix[0] * vec3(pass_position[0], 1.0);
    vec2 size = (pass_transformationMatrix[0] * vec3(pass_size[0], 0.0)).xy;

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

    color1 = pass_color1[0];
    color2 = rectData.color2[dataIndex];
    checkerboardSize = rectData.checkerboardSize[dataIndex].x;

    for (int i = 0; i < 4; i++) {
        relativePos = cornerOffsets[i] * size;
        vec3 screenPos = viewMatrix * vecToInt(position + vec3(relativePos, 0.0));
        gl_Position = vec4(screenPos.xy, pass_depth[0], 1.0);
        EmitVertex();
    }

    EndPrimitive();
}