#version 400 core

in int dataIndex;
in mat3 transformationMatrix;
in vec2 position;
in float depth;
in vec2 size;

out int pass_dataIndex;
out mat3 pass_transformationMatrix;
out vec2 pass_position;
out float pass_depth;
out vec2 pass_size;

void main(void) {
    pass_dataIndex = dataIndex;
    pass_transformationMatrix = transformationMatrix;
    pass_position = position;
    pass_depth = depth;
    pass_size = size;
}