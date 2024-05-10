/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;
precision lowp sampler2D;

in vec2 uv;
out vec4 color;

uniform sampler2D uTexture;
uniform vec2 uTexelSize;
uniform bool uAlpha;

uniform float uSize;

void main() {
    vec2 offset = (uTexelSize * uSize) + uTexelSize / 2;
    color += texture(uTexture, uv + offset);
    color += texture(uTexture, uv - offset);
    color += texture(uTexture, uv + vec2(offset.x, -offset.y));
    color += texture(uTexture, uv + vec2(-offset.x, offset.y));
    color *= .25;
    if (!uAlpha) {
        color.a = 1;
    }
}
