#version 400 core

in int charIndex;
in vec3 position;
in float textSize;
in vec3 color;

out int pass_charIndex;
out vec3 pass_position;
out float pass_textSize;
out vec3 pass_color;

void main(void) {
    pass_charIndex = charIndex;
    pass_position = position;
    pass_textSize = textSize;
    pass_color = color;
}