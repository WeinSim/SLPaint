#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

layout(std140)
uniform EllipseData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
} ellipseData;

uniform mat3 viewMatrix;

in vec3[] pass_position;
in vec2[] pass_size;
in vec4[] pass_color;
in int[] pass_dataIndex;

out vec2 relativePos;
out vec2 uvCoords;
out vec4 color;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

void main(void) {
    vec3 position = pass_position[0];
    vec2 size = pass_size[0];

    int dataIndex = pass_dataIndex[0];
    relativeBoundingBoxMin = ellipseData.boundingBox[dataIndex].xy - position.xy;
    relativeBoundingBoxMax = ellipseData.boundingBox[dataIndex].zw - position.xy;

    // first check if entire ellipse is outside of the bounding box
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

    color = pass_color[0];

    for (int i = 0; i < 4; i++) {
        vec2 offset = cornerOffsets[i];
        relativePos = offset * size;
        vec3 screenPos = viewMatrix * vec3(position.xy + relativePos, 1.0);
        gl_Position = vec4(screenPos.xy, position.z, 1.0);

        uvCoords = 2 * offset - 1;
        EmitVertex();
    }
    EndPrimitive();
}