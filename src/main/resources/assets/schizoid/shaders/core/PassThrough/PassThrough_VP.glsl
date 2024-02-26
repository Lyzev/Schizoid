/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 position;
out vec2 uv;

uniform float uScale;

void main() {
    gl_Position = vec4(position, 0, 1);
    uv = position * uScale * .5 + .5;
}