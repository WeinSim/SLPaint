#version 400 core

in vec2 position;
in vec2 textureCoords;
in vec2 size;

out vec2 pass_position;
out vec2 pass_textureCoords;
out vec2 pass_size;

void main(void) {
    pass_position = position;
    pass_textureCoords = textureCoords;
    pass_size = size;
}