/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 v_out_f2Uv;
out vec4 f_out_f4Color;

uniform sampler2D u_s2Texture;
uniform sampler2D u_s2Mask;
uniform bool u_bInvert;

void main() {
    if (u_bInvert) {
        if (texture(u_s2Mask, v_out_f2Uv).a == 0) {
            f_out_f4Color = texture(u_s2Texture, v_out_f2Uv);
        }
    } else {
        if (texture(u_s2Mask, v_out_f2Uv).a != 0) {
            f_out_f4Color = texture(u_s2Texture, v_out_f2Uv);
        }
    }
}
