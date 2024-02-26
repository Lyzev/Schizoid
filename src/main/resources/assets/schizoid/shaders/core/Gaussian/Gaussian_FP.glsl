/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;

in vec2 uv;
out vec4 color;

uniform sampler2D uTexture;
uniform vec2 uDirection;
uniform vec2 uTexelSize;
uniform bool uAlpha;

uniform vec3 uGaussian; // vec3(1.0 / (sqrt(2.0 * PI) * sigma), exp(-0.5 * delta * delta / (sigma * sigma)), u_f3Gaussian.y * u_f3Gaussian.y)
uniform float uSize; // sigma * 3
uniform float uPixelSkip;

void main() {
    vec3 gaussian = uGaussian;
    color += texture(uTexture, uv) * gaussian.x;
    float sum = gaussian.x;
    for (float i = 1; i <= uSize; i += uPixelSkip) {
        gaussian.xy *= gaussian.yz;
        vec2 offset = uTexelSize * i * uDirection;
        color += texture(uTexture, uv + offset) * gaussian.x;
        color += texture(uTexture, uv - offset) * gaussian.x;
        sum += gaussian.x * 2;
    }
    color /= sum;
    if (!uAlpha) {
        color.a = 1;
    }
}
