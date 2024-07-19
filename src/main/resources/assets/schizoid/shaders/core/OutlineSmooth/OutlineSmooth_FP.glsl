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

#define PI 3.14159265359

float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma)) / (sqrt(2.0 * PI) * sigma);
}

void main() {
    color = vec4(0.0);
    vec4 col = texture(Tex0, uv);
    if (col.a > 0.0) {
        float dist = distance(uv, col.xy) * length(ScreenSize);
        if (dist <= Length) {
            float sigma = Length / 3.0;
            color = vec4(gaussian(dist, sigma) / gaussian(0, sigma));
        }
    }
}
