/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 UV;
out vec4 Color;

uniform sampler2D Tex0;
uniform float Brightness;
uniform float Contrast;
uniform float Exposure;
uniform float Saturation;
uniform int Hue;
uniform float Temperature;
uniform vec3 Lift;
uniform vec3 Gamma;
uniform vec3 Gain;
uniform vec3 Offset;

#include "Luminance.glsl"
#include "Color.glsl"
#include "Temperature.glsl"
#include "FilmicTonemap.glsl"

void main()
{
    vec3 color = texture(Tex0, UV).rgb;
    color += Brightness;
    color = (color - .5) * Contrast + .5;
    color = (1 + Exposure) * color;
    color = luminosity(color, Saturation);
    color = hsv2rgb(rgb2hsv(color) + vec3(fract(Hue / 360.0), 0, 0));
    color *= vec3(1) / colorFromKelvin(Temperature);
    color = pow(max(vec3(0), color * (1 + Gain - Lift) + Lift + Offset), max(vec3(0), 1 - Gamma));
    Color = vec4(filmic(color), 1);
}
