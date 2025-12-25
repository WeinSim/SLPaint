#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

layout(std140)
uniform HSLData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
} hslData;

uniform mat3 viewMatrix;

in int[] pass_dataIndex;
in mat3[] pass_transformationMatrix;
in vec2[] pass_position;
in float[] pass_depth;
in vec2[] pass_size;

in vec3[] pass_color;
in int[] pass_hueSatAlpha;
in int[] pass_hsv;
in int[] pass_orientation;

out vec2 relativePos;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;
out vec2 uvCoords;
out vec3 color;
flat out int hueSatAlpha;
flat out int hsv;
flat out int orientation;

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
    relativeBoundingBoxMin = hslData.boundingBox[dataIndex].xy - position.xy;
    relativeBoundingBoxMax = hslData.boundingBox[dataIndex].zw - position.xy;

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

    color = pass_color[0];
    hueSatAlpha = pass_hueSatAlpha[0];
    hsv = pass_hsv[0];
    orientation = pass_orientation[0];

    for (int i = 0; i < 4; i++) {
        relativePos = cornerOffsets[i] * size;
        vec3 screenPos = viewMatrix * vecToInt(position + vec3(relativePos, 0.0));
        gl_Position = vec4(screenPos.xy, pass_depth[0], 1.0);
        uvCoords = cornerOffsets[i];
        EmitVertex();
    }

    EndPrimitive();
}