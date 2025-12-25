#version 400 core

in int dataIndex;
in mat3 transformationMatrix;
in vec2 position;
in float depth;
in vec2 size;

in vec3 color;
in int hueSatAlpha;
in int hsv;
in int orientation;

out int pass_dataIndex;
out mat3 pass_transformationMatrix;
out vec2 pass_position;
out float pass_depth;
out vec2 pass_size;

out vec3 pass_color;
out int pass_hueSatAlpha;
out int pass_hsv;
out int pass_orientation;

void main(void) {
    pass_dataIndex = dataIndex;
    pass_transformationMatrix = transformationMatrix;
    pass_position = position;
    pass_depth = depth;
    pass_size = size;

    pass_color = color;
    pass_hueSatAlpha = hueSatAlpha;
    pass_hsv = hsv;
    pass_orientation = orientation;
}