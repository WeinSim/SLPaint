#version 400 core

in int dataIndex;
in mat3 transformationMatrix;
in vec2 position;
in float depth;
in vec2 size;
in float hue;
in float saturation;
in int hueSatAlpha;
in int hsv;
in int orientation;
in vec3 fill;

out int pass_dataIndex;
out mat3 pass_transformationMatrix;
out vec2 pass_position;
out float pass_depth;
out vec2 pass_size;
out float pass_hue;
out float pass_saturation;
out int pass_hueSatAlpha;
out int pass_hsv;
out int pass_orientation;
out vec3 pass_fill;

void main(void) {
    pass_dataIndex = dataIndex;
    pass_transformationMatrix = transformationMatrix;
    pass_position = position;
    pass_depth = depth;
    pass_size = size;
    pass_hue = hue;
    pass_saturation = saturation;
    pass_hueSatAlpha = hueSatAlpha;
    pass_hsv = hsv;
    pass_orientation = orientation;
    pass_fill = fill;
}