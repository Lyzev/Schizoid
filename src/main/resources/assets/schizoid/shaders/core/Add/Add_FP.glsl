/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 v_out_f2Uv;
out vec4 f_out_f4Color;

uniform sampler2D u_s2Scene;
uniform sampler2D u_s2Texture;
uniform bool u_bAlpha;

void main() {
    f_out_f4Color = texture(u_s2Texture, v_out_f2Uv);
    if (f_out_f4Color.a == 0) {
        f_out_f4Color = texture(u_s2Scene, v_out_f2Uv);
    }
    if (!u_bAlpha && f_out_f4Color.a > 0) {
        f_out_f4Color.a = 1;
    }
}
