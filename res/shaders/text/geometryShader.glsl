#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in vec2[] pass_position;
in vec2[] pass_textureCoords;
in vec2[] pass_size;

out vec2 textureCoords;

// converts from "normal" screen coordinates (0, 0) to (w, h)
// to OpenGL coordinates (-1, 1) to (1, -1)
uniform mat3 viewMatrix;
uniform mat3 transformationMatrix;

const vec2 offsets[4] = vec2[4](
    vec2(0, 0),
    vec2(1, 0),
    vec2(0, 1),
    vec2(1, 1)
);

void main(void) {
    for (int i = 0; i < 4; i++) {
        vec3 screenPos = vec3(pass_position[0] + offsets[i] * pass_size[0], 1.0);
        screenPos = viewMatrix * transformationMatrix * screenPos;
        gl_Position = vec4(screenPos.xy, 0, 1);
        textureCoords = (pass_textureCoords[0] + pass_size[0] * offsets[i]) / vec2(256, 512);
        EmitVertex();
    }
    EndPrimitive();
}