#version 400 core

in vec2 textureCoords;
in vec3 color;

out vec4 outColor;

// uniform vec3 fill;
// uniform int doFill;

uniform sampler2D textureSamplers[4];

void main(void) {
    int page = int(floor(textureCoords.x));
    vec2 actualTextureCoords = vec2(textureCoords.x - page, textureCoords.y);

    vec4 textureColor = texture(textureSamplers[page], actualTextureCoords);
    float alpha = textureColor.r;

    outColor = vec4(color, alpha);

    // vec3 baseColor = vec3(page, page, page)  * 0.0000001;
    // outColor = vec4(baseColor, alpha * doFill);
    // outColor = vec4(alpha, alpha, alpha, 1.0);

    gl_FragDepth = gl_FragCoord.z;
}