/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform vec3 Primary;
uniform vec3 Secondary;
uniform vec3 Accent;
uniform float Threshold;

void main() {
    color = texture(Tex0, uv);
    if (distance(color.rgb, Primary) < Threshold) {
        color = vec4(color.rgb, 1.0);
    } else if (distance(color.rgb, Secondary) < Threshold) {
        color = vec4(color.rgb, 1.0);
    } else if (distance(color.rgb, Accent) < Threshold) {
        color = vec4(color.rgb, 1.0);
    } else {
        color = vec4(0.0);
    }
}
