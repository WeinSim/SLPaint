#version 400 core

in vec3 position;
in vec2 size;
in vec4 color1SW;
in int dataIndex;

out vec3 pass_position;
out vec2 pass_size;
out vec4 pass_color1SW;
out int pass_dataIndex;

void main(void) {
    pass_position = position;
    pass_size = size;
    pass_color1SW = color1SW;
    pass_dataIndex = dataIndex;
}