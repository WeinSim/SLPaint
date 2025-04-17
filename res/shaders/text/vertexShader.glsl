#version 400 core

in int charIndex;
in vec3 position;
in int textDataIndex;

out int pass_charIndex;
out vec3 pass_position;
out int pass_textDataIndex;

void main(void) {
    pass_charIndex = charIndex;
    pass_position = position;
    pass_textDataIndex = textDataIndex;
}