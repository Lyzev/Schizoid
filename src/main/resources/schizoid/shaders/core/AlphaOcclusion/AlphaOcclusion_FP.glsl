/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform sampler2D Tex1;
uniform vec4 Visible;
uniform vec4 Invisible;

void main() {
    vec4 mask = texture(Tex0, uv).rgba;
    vec3 scene = texture(Tex1, uv).rgb;
    if (mask.a > 0.0) {
        if (scene == mask.rgb) {
            color = Visible;
        } else {
            color = Invisible;
        }
    } else {
        color = vec4(0.0);
    }
}
