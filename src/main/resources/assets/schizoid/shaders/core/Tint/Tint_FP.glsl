/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform vec3 Color;
uniform bool RGBPuke;
uniform float Opacity;
uniform float Multiplier;
uniform float Time;
uniform float Yaw;
uniform float Pitch;

#include "Color.glsl"
#include "3DSimplexNoise.glsl"

void main() {
    color = texture(Tex0, uv);
    if (color.a > 0.0) {
        if (RGBPuke) {
            float time = Time / 8.0;
            vec2 pos = vec2(uv.x + Yaw / 180.0, uv.y - Pitch / 90.0);
            float d = snoise(vec3(pos, time));
            color.rgb = mix(color.rgb, hsv2rgb(vec3(mod(d * 0.5 - time, 1.0), 0.7, 1.0)), Opacity);
        } else {
            color.rgb = mix(color.rgb, Color, Opacity);
        }
        color.a = clamp(color.a * Multiplier, 0.0, 1.0);
    }
}
