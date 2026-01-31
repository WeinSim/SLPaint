#version 420 core

struct GroupData {
    // (x_min, y_min, x_max, y_max)
    vec4 boundingBox;
    vec4 color2;
    // If size == -1, then no checkerboard at all
    float checkerboardSize;
    // 3 * 4 bytes padding
};

layout(std140, binding = 0) uniform GroupAttributes {
    GroupData groupAttributes[256];
};

uniform mat3 viewMatrix;

// per vertex
in vec2 offset;
// per instance
in int gIndex; // index into groupAttributes
in mat3 transformationMatrix;
in vec2 position_in;
in float depth;
in vec2 size;
in vec4 color1_in;

out vec2 relativePos;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;
out vec4 color1;
out vec4 color2;
out float checkerboardSize;

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
    GroupData gData = groupAttributes[gIndex];

    vec2 basePos = (transformationMatrix * vec3(position_in, 1.0)).xy;
    relativePos = (transformationMatrix * vec3(size, 0.0)).xy;
    vec2 position = basePos + relativePos;

    relativeBoundingBoxMin = gData.boundingBox.xy - basePos;
    relativeBoundingBoxMax = gData.boundingBox.zw - basePos;

    color1 = color1_in;
    color2 = gData.color2;
    checkerboardSize = gData.checkerboardSize.x;

    gl_Position = vec4(position, depth, 1.0);
}