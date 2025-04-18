#version 400 core

in vec3 position;
in vec2 size;
in vec4 color;
in int dataIndex;

out vec3 pass_position;
out vec2 pass_size;
out vec4 pass_color;
out int pass_dataIndex;

void main(void) {
    pass_position = position;
    pass_size = size;
    pass_color = color;
    pass_dataIndex = dataIndex;
}