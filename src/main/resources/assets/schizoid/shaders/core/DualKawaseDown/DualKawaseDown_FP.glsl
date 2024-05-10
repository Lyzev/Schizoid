/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;
precision lowp sampler2D;

in vec2 uv;
out vec4 color;

uniform sampler2D uTexture;
uniform vec2 uHalfTexelSize;
uniform bool uAlpha;

uniform float uOffset;

void main() {
    color = (
        texture(uTexture, uv) * 4 +
        texture(uTexture, uv - uHalfTexelSize.xy * uOffset) +
        texture(uTexture, uv + uHalfTexelSize.xy * uOffset) +
        texture(uTexture, uv + vec2(uHalfTexelSize.x, -uHalfTexelSize.y) * uOffset) +
        texture(uTexture, uv - vec2(uHalfTexelSize.x, -uHalfTexelSize.y) * uOffset)
    ) / 8;
    if (!uAlpha) {
        color.a = 1;
    }
}
