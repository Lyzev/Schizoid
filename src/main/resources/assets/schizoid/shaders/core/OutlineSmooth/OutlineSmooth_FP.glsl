/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform float Length;
uniform vec2 ScreenSize;

#include "Gaussian.glsl"

/*
 * This is the fragment shader for the smooth outline effect using the Jump Flooding algorithm output and Gaussian normal distribution.
 */
void main() {
    color = vec4(0.0);
    vec4 col = texture(Tex0, uv);
    // Check if the sampled alpha value is greater than 0.0
    if (col.a > 0.0) {
        // Compute the distance between the current point and the texture coordinate stored in col.xy
        float delta = distance(uv * ScreenSize, col.xy * ScreenSize);
        // Check if the distance is less than the outline length
        if (delta <= Length) {
            // Compute the sigma value for the Gaussian normal distribution
            float sigma = Length / 3.0;// 3-sigma rule aka. 68-95-99.7 rule (see https://en.wikipedia.org/wiki/68%E2%80%9395%E2%80%9399.7_rule)
            // Set the output color with smooth alpha transition using the Gaussian normal distribution
            color = vec4(gaussian(delta, sigma) / gaussian(0, sigma));// Normalize the Gaussian normal distribution
        }
    }
}
