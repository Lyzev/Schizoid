/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform bool Alpha;

void main() {
    color = texture(Tex0, uv);
    if (!Alpha && color.a > 0.0) {
        color.a = 1.0;
    }
}
