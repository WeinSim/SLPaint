#version 400 core

in int dataIndex;
in vec2 position;
in float depth;
in int charIndex;

out int pass_dataIndex;
out vec2 pass_position;
out float pass_depth;
out int pass_charIndex;

void main(void) {
    pass_dataIndex = dataIndex;
    pass_position = position;
    pass_depth = depth;
    pass_charIndex = charIndex;
}