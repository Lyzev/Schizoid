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

uniform int uSize;

void main() {
    int size = uSize * 2 + 1;
    for (int i = -uSize; i <= uSize; i++) {
        color += texture(uTexture, uv + uTexelSize * i * uDirection) / size;
    }
    if (!uAlpha) {
        color.a = 1;
    }
}
