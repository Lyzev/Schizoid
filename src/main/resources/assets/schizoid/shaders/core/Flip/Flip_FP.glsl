/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;

void main() {
    color = vec4(texture(Tex0, vec2(uv.x, 1.0 - uv.y)).rgb, 1.0);
}
