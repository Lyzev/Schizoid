/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

#include "Noise.glsl"
#include "Luminance.glsl"

uniform sampler2D scene;
uniform sampler2D mask;
uniform vec2 texelSize;
uniform float time;

void main() {
    if (texture(mask, uv).a == 0) {
        if (texture(mask, uv + texelSize.y).a > 0) {
            color = vec4(vec3(0), 0.2);
        } else if (texture(mask, uv - texelSize.y).a > 0) {
            color = vec4(vec3(0), 0.2);
        } else if (texture(mask, uv + texelSize.x).a > 0) {
            color = vec4(vec3(0), 0.2);
        } else if (texture(mask, uv - texelSize.x).a > 0) {
            color = vec4(vec3(0), 0.2);
        } else {
            color = vec4(0);
        }
        return;
    }

    vec2 distortedUv = uv + (rand(uv + time) - 0.5) * 0.1;
    color = texture(scene, distortedUv);
    color.rgb = luminosity(color.rgb, 1.4);
    color.a = 1;
}
