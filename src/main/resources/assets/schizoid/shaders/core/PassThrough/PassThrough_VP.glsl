/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 position;
out vec2 uv;

uniform float Scale;

void main() {
    gl_Position = vec4(position, 0.0, 1.0);
    uv = position * Scale * 0.5 + 0.5;
}
