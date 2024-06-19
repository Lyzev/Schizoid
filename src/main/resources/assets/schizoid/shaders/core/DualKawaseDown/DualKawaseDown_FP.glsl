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
    texture(Tex0, uv) * 4.0 +
    texture(Tex0, uv - HalfTexelSize.xy * Offset) +
    texture(Tex0, uv + HalfTexelSize.xy * Offset) +
    texture(Tex0, uv + vec2(HalfTexelSize.x, -HalfTexelSize.y) * Offset) +
    texture(Tex0, uv - vec2(HalfTexelSize.x, -HalfTexelSize.y) * Offset)
    ) / 8.0;
    if (!Alpha) {
        color.a = 1.0;
    }
}
