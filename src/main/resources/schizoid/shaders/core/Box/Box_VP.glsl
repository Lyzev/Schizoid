/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;

in vec2 position;
out vec2 uv;

void main() {
    gl_Position = vec4(position, 0.0, 1.0);
    uv = position * 0.5 + 0.5;
}
