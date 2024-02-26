/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;

in vec2 uv;
out vec4 color;

uniform sampler2D uTexture;
uniform vec4 uColor;
uniform float uOpacity;
uniform bool uRGPuke;
uniform float uTime;

#include "Acrylic.glsl"

void main() {
    color = texture(uTexture, uv);
    if (color.a > 0) {
        if (uRGPuke) {
            float d = sqrt(pow(uv.x + .2, 2) + pow(uv.y - .2, 2));
            color.rgb = mix(color.rgb, hsb2rgb(vec3(d * .5 - uTime / 4, 0.7, 1)), uOpacity);
        } else {
            color.rgb = mix(color.rgb, uColor.rgb, uOpacity);
            color.a *= uColor.a;
        }
    }
}
