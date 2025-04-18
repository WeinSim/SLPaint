#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in int[] pass_charIndex;
in vec3[] pass_position;
in int[] pass_dataIndex;

out vec2 position;
out vec2 textureCoords;
out vec3 color;
out vec2 boundingBoxMin;
out vec2 boundingBoxMax;

// converts from "normal" screen coordinates (0, 0) to (w, h)
// to OpenGL coordinates (-1, 1) to (1, -1)
uniform mat3 viewMatrix;
uniform vec2 textureSize;

layout(std140)
uniform TextData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
    // (r, g, b, size)
    vec4[256] colorSize;
} textData;

layout(std140)
uniform FontData {
    // (x, y, w, h)
    vec4[256] fontData;
} fontData;

const vec2 offsets[4] = vec2[4](
    vec2(0, 0),
    vec2(1, 0),
    vec2(0, 1),
    vec2(1, 1)
);

void main(void) {
    int charIndex = pass_charIndex[0];
    vec2 baseTextureCoords = fontData.fontData[charIndex].xy;
    vec2 textureOffset = fontData.fontData[charIndex].zw;

    int dataIndex = pass_dataIndex[0];
    float textSize = textData.colorSize[dataIndex].w;
    color = textData.colorSize[dataIndex].xyz;

    boundingBoxMin = textData.boundingBox[dataIndex].xy;
    boundingBoxMax = textData.boundingBox[dataIndex].zw;

    // first check if entire glyph is outside of the bounding box
    vec2 basePos = pass_position[0].xy;
    vec2 maxOffset = textureOffset * textSize;
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

    for (int i = 0; i < 4; i++) {
        vec3 screenPos = vec3(basePos + offsets[i] * textureOffset * textSize, 1.0);
        position = screenPos.xy;
        screenPos = viewMatrix * screenPos;
        gl_Position = vec4(screenPos.xy, pass_position[0].z, 1.0);

        textureCoords = (baseTextureCoords + textureOffset * offsets[i]) / textureSize;

        EmitVertex();
    }
    EndPrimitive();
}