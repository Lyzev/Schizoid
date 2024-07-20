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

#include "Distance.glsl"

/*
 * This is the step fragment shader for the Jump Flooding algorithm.
 */
void main() {
    color = vec4(0.0);
    float minDistance = distanceSquared(vec2(0.0), 1 / TexelSize);// Calculate the maximum distance
    // Loop through the 3x3 neighborhood (including the current pixel)
    for (int x = -1; x <= 1; x ++) {
        for (int y = -1; y <= 1; y ++) {
            vec2 offset = vec2(float(x), float(y)) * float(Length) * TexelSize;
            vec4 col = texture(Tex0, uv + offset);
            // Check if the sampled alpha value is greater than 0.0, because that means there is a uv stored in the fragment
            if (col.a > 0.0) {
                // Calculate the distance between the current pixel and the stored uv
                float distance = distanceSquared(uv * (1 / TexelSize), col.xy * (1 / TexelSize));
                // Check if the distance is less than the minimum distance
                if (distance <= minDistance) {
                    minDistance = distance;
                    // Store the nearest point's coordinates in the output fragment
                    color = vec4(col.xy, 0.0, 1.0);
                }
            }
        }
    }
}
