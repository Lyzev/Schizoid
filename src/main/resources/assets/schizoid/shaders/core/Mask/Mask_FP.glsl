/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform sampler2D Tex1;
uniform bool Invert;

void main() {
    color = vec4(0.0);
    if (Invert) {
        if (texture(Tex1, uv).a == 0.0) {
            color = texture(Tex0, uv);
        }
    } else {
        if (texture(Tex1, uv).a != 0.0) {
            color = texture(Tex0, uv);
        }
    }
}
