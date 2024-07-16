/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform vec4 Color;
uniform int Length;
uniform vec2 TexelSize;

void main() {
    color = texture(Tex0, uv);
    if (color.a > 0.0) {
        return;
    }
    for (int x = -Length; x <= Length; x += Length) {
        for (int y = -Length; y <= Length; y += Length) {
            if (x == 0 && y == 0) {
                continue;
            }
            vec2 offset = vec2(x, y) * TexelSize;
            vec4 col = texture(Tex0, uv + offset);
            if (col.a > 0.0) {
                color = Color;
                return;
            }
        }
    }
}
