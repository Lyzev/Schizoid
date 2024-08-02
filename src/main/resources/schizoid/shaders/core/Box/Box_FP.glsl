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
uniform vec2 Direction;
uniform vec2 TexelSize;
uniform int Size;
uniform bool Alpha;

void main() {
    int size = Size * 2 + 1;
    for (int i = -Size; i <= Size; i++) {
        color += texture(Tex0, uv + TexelSize * i * Direction) / size;
    }
    if (!Alpha) {
        color.a = 1.0;
    }
}
