/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D scene;
uniform float threshold;

#include "Luminance.glsl"

void main() {
    color = texture(scene, uv);

    // check whether fragment output is higher than threshold, if so output as brightness color
    float brightness = luminance(color.rgb);
    if (brightness > threshold) {
        color = vec4(color.rgb, 1);
    } else {
        color = vec4(0, 0, 0, 0);
    }
}
