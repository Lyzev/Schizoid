/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D scene;

void main() {
    color = vec4(texture(scene, vec2(uv.x, 1 - uv.y)).rgb, 1);
}
