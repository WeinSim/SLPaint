#version 400 core

in vec2 passUVCoords;

out vec4 outColor;

uniform float hue;
uniform float saturation;
// 0 = lightness (1-dim), 1 = hue-sat (2-dim), 2 = alpha (1-dim)
uniform int hueSatAlpha;
// 0 = hsl, 1 = hsv
uniform int hsv;
// 0 = vertical gradient, 1 = horizontal gradient
uniform int orientation;
uniform vec3 fill;

vec3 hsvToRGB(float h, float s, float v);

vec3 hslToRGB(float h, float s, float l);

float mag(vec2 v);

float atan2(float y, float x);

const float PI = 3.1415926535;

void main(void) {
    if (hueSatAlpha == 0) {
        if (orientation == 0) {
            outColor = vec4(hsv == 1
                    ? hsvToRGB(hue, saturation, 1 - passUVCoords.y)
                    : hslToRGB(hue, saturation, 1 - passUVCoords.y),
                1.0);
        } else {
            outColor = vec4(hsv == 1
                    ? hsvToRGB(hue, saturation, passUVCoords.x)
                    : hslToRGB(hue, saturation, passUVCoords.x),
                1.0);
        }
    } else if (hueSatAlpha == 1) {
        outColor = vec4(hsv == 1
                ? hsvToRGB(passUVCoords.x * 360, 1 - passUVCoords.y, 1.0)
                : hslToRGB(passUVCoords.x * 360, 1 - passUVCoords.y, 0.5),
            1.0);
    } else if (hueSatAlpha == 2) {
        if (orientation == 0) {
            outColor = vec4(fill, 1 - passUVCoords.y);
        } else {
            outColor = vec4(fill, passUVCoords.x);
        }
    } else {
        vec2 transformedUVCoords = 2 * passUVCoords - 1;
        float mag = mag(transformedUVCoords);
        if (mag > 1) {
            discard;
        }
        float angle = atan2(transformedUVCoords.y, transformedUVCoords.x);
        outColor = vec4(hsv == 1
                ? hsvToRGB(angle * 180 / PI, mag, 1.0)
                : hslToRGB(angle * 180 / PI, mag, 0.5),
            1.0);
    }

    gl_FragDepth = gl_FragCoord.z;
}

float mag(vec2 v) {
    return sqrt(v.x * v.x + v.y * v.y);
}

float atan2(float y, float x) {
    if (abs(x) < 1e-6) {
        return y < 0 ? 3 * PI / 2 : PI / 2;
    }
    if (x > 0) {
        return atan(y / x);
    } else {
        return atan(y / x) - PI;
    }
}

vec3 hslToRGB(float h, float s, float l) {
    h = mod(mod(h, 360) + 360, 360);
    float c = (1 - abs(2 * l - 1)) * s;
    float x = c * (1 - abs(mod(h / 60, 2) - 1));
    float m = l - c / 2;
    float r, g, b;
    if (h < 60) {
        r = c;
        g = x;
        b = 0;
    } else if (h < 120) {
        r = x;
        g = c;
        b = 0;
    } else if (h < 180) {
        r = 0;
        g = c;
        b = x;
    } else if (h < 240) {
        r = 0;
        g = x;
        b = c;
    } else if (h < 300) {
        r = x;
        g = 0;
        b = c;
    } else {
        r = c;
        g = 0;
        b = x;
    }
    r = (r + m);
    g = (g + m);
    b = (b + m);
    return vec3(r, g, b);
}

vec3 hsvToRGB(float h, float s, float v) {
    h = mod(mod(h, 360) + 360, 360);
    float c = v * s;
    float x = c * (1 - abs(mod(h / 60, 2) - 1));
    float m = v - c;
    float r, g, b;
    if (h < 60) {
        r = c;
        g = x;
        b = 0;
    } else if (h < 120) {
        r = x;
        g = c;
        b = 0;
    } else if (h < 180) {
        r = 0;
        g = c;
        b = x;
    } else if (h < 240) {
        r = 0;
        g = x;
        b = c;
    } else if (h < 300) {
        r = x;
        g = 0;
        b = c;
    } else {
        r = c;
        g = 0;
        b = x;
    }
    r = (r + m);
    g = (g + m);
    b = (b + m);
    return vec3(r, g, b);
}