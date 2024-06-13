/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;
precision lowp sampler2D;

in vec2 uv;
out vec4 color;

uniform sampler2D Texture;
uniform vec2 TexelSize;
uniform bool Alpha;

uniform float Size;

void main() {
    vec2 offset = (TexelSize * Size) + TexelSize / 2.0;
    color += texture(Texture, uv + offset);
    color += texture(Texture, uv - offset);
    color += texture(Texture, uv + vec2(offset.x, -offset.y));
    color += texture(Texture, uv + vec2(-offset.x, offset.y));
    color *= 0.25;
    if (!Alpha) {
        color.a = 1.0;
    }
}
