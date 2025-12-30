#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

// IMPORTANT: this uniform block (TextData) has to come before the other uniform
// block (FontData) in order for the shader to automatically recognize its name
// and size.
layout(std140)
uniform TextData {
    // (x_min, y_min, x_max, y_max)
    vec4[256] boundingBox;
    vec4[256] color;
    // (t.m00, t.m01, t.m02, size)
    vec4[256] transformationMatrix0Size;
    vec4[256] transformationMatrix1;
    vec4[256] transformationMatrix2;
} textData;

layout(std140)
uniform FontData {
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

    int dataIndex = pass_dataIndex[0];
    float relativeTextSize = textData.transformationMatrix0Size[dataIndex].w;

    mat3 transformationMatrix = mat3(
        textData.transformationMatrix0Size[dataIndex].xyz,
        textData.transformationMatrix1[dataIndex].xyz,
        textData.transformationMatrix2[dataIndex].xyz
    );

    boundingBoxMin = textData.boundingBox[dataIndex].xy;
    boundingBoxMax = textData.boundingBox[dataIndex].zw;

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

    color = textData.color[dataIndex];

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