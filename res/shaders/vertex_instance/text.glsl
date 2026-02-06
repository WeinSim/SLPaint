#version 420 core

struct GroupData {
    vec2 boundingBoxMin;
    vec2 boundingBoxMax;
    vec4 color;
    mat3 transformationMatrix;
    float relativeTextSize;
    // 3 * 4 bytes padding
};

layout(std140, binding = 1) uniform GroupAttributes {
    GroupData groupAttributes[256];
};

layout(std140, binding = 0) uniform FontData {
    // (x, y, w, h)
    vec4[256] fontData;
};

uniform mat3 viewMatrix;
// TODO: why isn't this part of FontData?
uniform vec2 textureSize;

// per vertex
in vec2 cornerPos;
// per instance
in int gIndex;
in vec2 position;
in float depth;
in int charIndex;

out vec2 relativePos;
out vec2 textureCoords;
out vec4 color;
out vec2 relativeBoundingBoxMin;
out vec2 relativeBoundingBoxMax;

vec4 getGLPos(vec3 screenPos, float depth) {
    screenPos.x = floor(screenPos.x);
    screenPos.y = floor(screenPos.y);
    return vec4(
        (viewMatrix * screenPos).xy,
        depth,
        1.0
    );
}

void main(void) {
    GroupData gData = groupAttributes[gIndex];

    vec2 textureOffset = fontData[charIndex].zw;
    vec3 basePos = gData.transformationMatrix * vec3(position, 1.0);
    relativePos = (
        gData.transformationMatrix * vec3(
            cornerPos * textureOffset * gData.relativeTextSize,
        0.0)
    ).xy;
    gl_Position = getGLPos(basePos + vec3(relativePos, 0.0), depth);

    color = gData.color;
    textureCoords = (fontData[charIndex].xy + cornerPos * textureOffset) / textureSize;

    relativeBoundingBoxMin = gData.boundingBoxMin - basePos.xy;
    relativeBoundingBoxMax = gData.boundingBoxMax - basePos.xy;
}