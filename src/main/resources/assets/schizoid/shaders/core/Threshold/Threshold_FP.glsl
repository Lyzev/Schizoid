/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D scene;
uniform vec3 primary;
uniform vec3 secondary;
uniform vec3 accent;

void main() {
    color = texture(scene, uv);
    if (distance(color.rgb, primary) < .2) {
        color = vec4(color.rgb, 1);
    } else if (distance(color.rgb, secondary) < .2) {
        color = vec4(color.rgb, 1);
    } else if (distance(color.rgb, accent) < .2) {
        color = vec4(color.rgb, 1);
    } else {
        color = vec4(0);
    }
}
