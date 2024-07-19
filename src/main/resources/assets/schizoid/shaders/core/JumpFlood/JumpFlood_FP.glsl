/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision highp float;

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform int Length;
uniform vec2 TexelSize;

float distSquared(vec2 A, vec2 B) {
    vec2 C = A - B;
    return dot(C, C);
}

void main() {
    color = vec4(0.0);
    float minDistance = 1e10; // Set to a large value to find the minimum distance
    for (int x = -1; x <= 1; x ++) {
        for (int y = -1; y <= 1; y ++) {
            vec2 offset = vec2(float(x), float(y)) * float(Length) * TexelSize;
            vec4 col = texture(Tex0, uv + offset);
            if (col.a > 0.0) {
                float distance = distSquared(uv, col.xy);
                if (distance < minDistance) {
                    minDistance = distance;
                    color = vec4(col.xy, 0.0, 1.0); // Store the nearest point's coordinates
                }
            }
        }
    }
}
