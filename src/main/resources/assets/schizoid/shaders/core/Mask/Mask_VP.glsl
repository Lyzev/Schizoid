/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 v_in_f2Position;
out vec2 v_out_f2Uv;

void main() {
    gl_Position = vec4(v_in_f2Position, 0, 1);
    v_out_f2Uv = v_in_f2Position * .5 + .5;
}