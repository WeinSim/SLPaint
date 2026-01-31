#version 420 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct GroupData {
    // (x_min, y_min, x_max, y_max)
    vec4 boundingBox;
    vec4 color;
    mat3 transformationMatrix;
    float relativeTextSize;
    // 3 * 4 bytes padding
};

layout(std140, binding = 1) uniform GroupAttributes {
    GroupData gData[256];
};

layout(std140, binding = 0) uniform FontData {
    // (x, y, w, h)
    vec4[256] fontData;
} fontData;

const vec2 cornerOffsets[4] = vec2[4](
    vec2(0, 0),
    vec2(1, 0),
    vec2(0, 1),
    vec2(1, 1)
);

// converts from "normal" screen coordinates (0, 0) to (w, h)
// to OpenGL coordinates (-1, 1) to (1, -1)
uniform mat3 viewMatrix;
uniform vec2 textureSize;

in int[] pass_dataIndex;
in vec2[] pass_position;
in float[] pass_depth;
in int[] pass_charIndex;

out vec2 position;
out vec2 textureCoords;
out vec4 color;
out vec2 boundingBoxMin;
out vec2 boundingBoxMax;

void main(void) {
    int charIndex = pass_charIndex[0];
    vec2 baseTextureCoords = fontData.fontData[charIndex].xy;
    vec2 textureOffset = fontData.fontData[charIndex].zw;

    // baseTextureCoords = vec2(0, 0);
    // textureOffset = vec2(32, 32);

    int dataIndex = pass_dataIndex[0];
    float relativeTextSize = gData[dataIndex].relativeTextSize;

    mat3 transformationMatrix = gData[dataIndex].transformationMatrix;

    boundingBoxMin = gData[dataIndex].boundingBox.xy;
    boundingBoxMax = gData[dataIndex].boundingBox.zw;

    // first check if entire glyph is outside of the bounding box
    vec2 basePos = (transformationMatrix * vec3(pass_position[0], 1.0)).xy;
    vec2 maxOffset = (transformationMatrix * vec3(textureOffset * relativeTextSize, 0.0)).xy;

    if (basePos.x > boundingBoxMax.x) {
        return;
    }
    if (basePos.y > boundingBoxMax.y) {
        return;
    }
    if (basePos.x + maxOffset.x < boundingBoxMin.x) {
        return;
    }
    if (basePos.y + maxOffset.y < boundingBoxMin.y) {
        return;
    }

    color = gData[dataIndex].color;

    for (int i = 0; i < 4; i++) {
        vec3 screenPos = vec3(basePos + cornerOffsets[i] * textureOffset * relativeTextSize, 1.0);
        position = screenPos.xy;
        screenPos = viewMatrix * screenPos;
        gl_Position = vec4(screenPos.xy, pass_depth[0], 1.0);

        textureCoords = (baseTextureCoords + cornerOffsets[i] * textureOffset) / textureSize;

        EmitVertex();
    }

    EndPrimitive();
}
