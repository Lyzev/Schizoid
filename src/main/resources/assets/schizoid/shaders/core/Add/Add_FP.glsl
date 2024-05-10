/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D uScene;
uniform sampler2D uTexture;
uniform bool uAlpha;

void main() {
    color = texture(uTexture, uv);
    if (color.a == 0) {
        color = texture(uScene, uv);
    }
    if (!uAlpha && color.a > 0) {
        color.a = 1;
    }
}
