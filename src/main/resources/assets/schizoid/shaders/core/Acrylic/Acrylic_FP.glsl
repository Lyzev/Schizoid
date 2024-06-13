/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision highp float;

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;

uniform float Luminosity;
uniform float NoiseStrength;
uniform float NoiseScale;
uniform float Opacity;
uniform bool RGBPuke;
uniform float RGBPukeOpacity;
uniform float Time;
uniform float Yaw;
uniform float Pitch;

#include "Color.glsl"
#include "3DSimplexNoise.glsl"
#include "Noise.glsl"
#include "Luminance.glsl"

void main() {
    color = texture(Tex0, uv);

    if (Luminosity != 1.0) {
        color.rgb = luminosity(color.rgb, Luminosity);
    }

    if (NoiseStrength > 0.0) {
        color.rgb = mix(color.rgb, noiseTile(uv, NoiseScale), NoiseStrength);
    }

    if (RGBPuke && RGBPukeOpacity > 0.0) {
        float time = Time / 8.0;
        vec2 pos = vec2(uv.x + Yaw / 180.0, uv.y - Pitch / 90.0);
        float d = snoise(vec3(pos, time));
        color.rgb = mix(color.rgb, hsv2rgb(vec3(mod(d * 0.5 - time, 1.0), 0.7, 1.0)), RGBPukeOpacity);
    }

    if (Opacity != -1.0) {
        color.a = Opacity;
    }
}
