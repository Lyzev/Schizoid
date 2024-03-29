/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision lowp float;

in vec2 uv;
out vec4 color;

uniform sampler2D scene;
uniform vec2 direction;
uniform vec2 texelSize;
uniform bool alpha;

uniform vec3 gaussian; // "Incremental Computation of the Gaussian" by Ken Turkowski
uniform int support; // ceil(sigma * 3)
uniform bool linearSampling;

void main() {
    vec3 gauss = gaussian;
    color = texture(scene, uv) * gauss.x;
    float sum = gauss.x;
    if (linearSampling) {
        for (float i = 1; i <= support; i += 2) {
            gauss.xy *= gauss.yz;
            float w1 = gauss.x;
            gauss.xy *= gauss.yz;
            float w2 = gauss.x;
            float w = w1 + w2;
            vec2 offset = texelSize * direction * ((i * w1 + (i + 1) * w2) / w);
            color += texture(scene, uv + offset) * w;
            color += texture(scene, uv - offset) * w;
            sum += w * 2;
        }
    } else {
        for (float i = 1; i <= support; i++) {
            gauss.xy *= gauss.yz;
            vec2 offset = texelSize * direction * i;
            color += texture(scene, uv + offset) * gauss.x;
            color += texture(scene, uv - offset) * gauss.x;
            sum += gauss.x * 2;
        }
    }
    color /= sum;
    if (!alpha) {
        color.a = 1;
    }
}
