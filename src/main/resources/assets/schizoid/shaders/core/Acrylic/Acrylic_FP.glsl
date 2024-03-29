/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;

in vec2 uv;
out vec4 color;

uniform sampler2D uTexture;

uniform float uLuminosity;
uniform float uNoiseStrength;
uniform float uNoiseScale;
uniform float uOpacity;
uniform bool uRGBPuke;
uniform float uRGBPukeOpacity;
uniform float uTime;

#include "Acrylic.glsl"
#include "Luminance.glsl"

void main() {
    color = texture(uTexture, uv);

    if (uLuminosity != 1) {
        color.rgb = luminosity(color.rgb, uLuminosity);
    }

    if (uNoiseStrength > 0) {
        color.rgb = mix(color.rgb, noiseTile(uv, uNoiseScale), uNoiseStrength);
    }

    if (uRGBPuke && uRGBPukeOpacity > 0) {
        float d = length(uv);
        color.rgb = mix(color.rgb, hsv2rgb(vec3(mod(d * .5 - uTime / 8, 1), .7, 1)), uRGBPukeOpacity);
    }

    if (uOpacity != -1) {
        color.a = uOpacity;
    }
}
