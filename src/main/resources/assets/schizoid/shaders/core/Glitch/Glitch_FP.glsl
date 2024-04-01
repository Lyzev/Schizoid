/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D scene;
uniform sampler2D mask;
uniform vec2 texelSize;
uniform float time;
uniform float rate;

#include "Luminance.glsl"

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453) * 2.0 - 1.0;
}

float offset(float blocks, vec2 uv) {
    float shaderTime = time*rate;
    return rand(vec2(shaderTime, floor(uv.y * blocks)));
}

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

    color.r = texture(scene, uv + vec2(offset(64.0, uv) * 0.03, 0.0)).r;
    color.g = texture(scene, uv + vec2(offset(64.0, uv) * 0.03 * 0.16666666, 0.0)).g;
    color.b = texture(scene, uv + vec2(offset(64.0, uv) * 0.03, 0.0)).b;
    color.rgb = luminosity(color.rgb, 1.4);
    color.a = 1.0;
}
