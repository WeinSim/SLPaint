#version 150 core

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out vec2 passUVCoords;

uniform vec3 viewportTopLeft;
uniform vec3 viewportBottomRight;

uniform mat3 viewMatrix;
uniform mat3 transformationMatrix;

const vec2[4] cornerOffsets = vec2[4](
    vec2(0, 0),
    vec2(0, 1),
    vec2(1, 0),
    vec2(1, 1)
);

void main(void) {
    vec3 topLeft = transformationMatrix * vec3(0, 0, 1.0);
    vec3 bottomRight = transformationMatrix * vec3(1, 1, 1.0);
    if (bottomRight.x < viewportTopLeft.x) {
        return;
    }
    if (topLeft.x > viewportBottomRight.x) {
        return;
    }
    if (bottomRight.y < viewportTopLeft.y) {
        return;
    }
    if (topLeft.y > viewportBottomRight.y) {
        return;
    }
    float width = bottomRight.x - topLeft.x;
    float height = bottomRight.y - topLeft.y;

    for (int i = 0; i < 4; i++) {
        vec3 oldScreenPos = transformationMatrix * vec3(cornerOffsets[i], 1.0);
        vec3 screenPos = oldScreenPos;
        screenPos = min(max(viewportTopLeft, screenPos), viewportBottomRight);
        passUVCoords = ((screenPos - topLeft) / vec3(width, height, 1.0)).xy;
        // passUVCoords = cornerOffsets[i];
        screenPos = viewMatrix * vec3(screenPos.xy, 1.0);
        gl_Position = vec4(screenPos.xy, 0, 1);
        EmitVertex();
    }
    EndPrimitive();
}