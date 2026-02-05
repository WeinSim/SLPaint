#version 420 core

struct GroupData {
    // (x_min, y_min, x_max, y_max)
    // vec4 boundingBox;
    vec2 boundingBoxMin;
    vec2 boundingBoxMax;
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
in vec2 position;
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

    vec3 position3 = vec3(position, 1.0);
    vec3 pos = transformationMatrix * (position3 + vec3(size * offset, 0.0));
    vec2 basePos = (transformationMatrix * position3).xy;
    relativePos = pos.xy - basePos;

    // basePos = position_in;
    // relativePos = size * offset;
    vec2 screenPos = (viewMatrix * pos).xy;

    gl_Position = vec4(screenPos, depth, 1.0);

    relativeBoundingBoxMin = gData.boundingBoxMin - basePos.xy;
    relativeBoundingBoxMax = gData.boundingBoxMax - basePos.xy;

    color1 = color1_in;
    color2 = gData.color2;
    checkerboardSize = gData.checkerboardSize;
}