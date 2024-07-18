/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform float Length;
uniform vec2 ScreenSize;

float distSquared(vec2 A, vec2 B) {
    vec2 C = A - B;
    return dot(C, C);
}

void main() {
    color = vec4(0.0);
    vec4 col = texture(Tex0, uv);
    if (col.a > 0.0) {
        float distance = distSquared(uv * ScreenSize, col.xy * ScreenSize);
        if (distance <= Length * Length) {
            color = vec4(1.0);
        }
    }
}
