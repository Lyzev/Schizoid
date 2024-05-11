/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 Position;
out vec2 UV;

void main() {
    gl_Position = vec4(Position, 0, 1);
    UV = Position * .5 + .5;
}
