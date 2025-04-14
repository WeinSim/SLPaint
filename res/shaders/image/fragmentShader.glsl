#version 400 core

in vec2 passUVCoords;

out vec4 outColor;

uniform sampler2D textureSampler;

void main(void) {

    vec4 sampleColor = texture(textureSampler, passUVCoords);

    float alpha = sampleColor.a;
    // outColor = vec4(alpha, alpha, alpha, 1.0);
    outColor = sampleColor;

    gl_FragDepth = gl_FragCoord.z;
}