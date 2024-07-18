/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision highp float;

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;

void main() {
    if (texture(Tex0, uv).a > 0.0) {
        color = vec4(uv, 0.0, 1.0);
    } else {
        color = vec4(0.0);
    }
}
