/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;
precision lowp sampler2D;

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform vec2 HalfTexelSize;
uniform float Offset;
uniform bool Alpha;

void main() {
    color = (
        texture(Tex0, uv + vec2(- HalfTexelSize.x * 2.0, 0.0) * Offset) +
        texture(Tex0, uv + vec2(- HalfTexelSize.x, HalfTexelSize.y) * Offset) * 2.0 +
        texture(Tex0, uv + vec2(0.0, HalfTexelSize.y * 2.0) * Offset) +
        texture(Tex0, uv + HalfTexelSize * Offset) * 2.0 +
        texture(Tex0, uv + vec2(HalfTexelSize.x * 2.0, 0.0) * Offset) +
        texture(Tex0, uv + vec2(HalfTexelSize.x, -HalfTexelSize.y) * Offset) * 2.0 +
        texture(Tex0, uv + vec2(0.0, -HalfTexelSize.y * 2.0) * Offset) +
        texture(Tex0, uv - HalfTexelSize * Offset) * 2.0
    ) / 12.0;
    if (!Alpha) {
        color.a = 1.0;
    }
}
