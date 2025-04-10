#version 400 core

in vec2 textureCoords;
in float page;

out vec4 outColor;

uniform vec3 fill;
uniform int doFill;

uniform sampler2D textureSamplers[4];

void main(void) {
    vec4 textureColor = texture(textureSamplers[int(page)], textureCoords);
    float alpha = textureColor.r;

    // outColor = vec4(fill, alpha * doFill);
    vec3 baseColor = vec3(page, page, page)  * 0.0000001;
    outColor = vec4(baseColor, alpha * doFill);
    // outColor = vec4(alpha, alpha, alpha, 1.0);
}