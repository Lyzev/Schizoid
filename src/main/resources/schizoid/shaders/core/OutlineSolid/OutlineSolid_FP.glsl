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

#include "Distance.glsl"

/*
 * This is the fragment shader for the solid outline effect using the Jump Flooding algorithm output.
 */
void main() {
    color = vec4(0.0);
    vec4 col = texture(Tex0, uv);
    // Check if the sampled alpha value is greater than 0.0
    if (col.a > 0.0) {
        // Compute the squared distance between the current point and the texture coordinate stored in col.xy
        float distance = distanceSquared(uv * ScreenSize, col.xy * ScreenSize);

        // Compute the squared length of the outline
        float lengthSquared = Length * Length + Length * Length;

        // If the squared distance is less than the squared length, set the color to white
        if (distance <= lengthSquared + 1.0) {
            color = vec4(1.0);
        }
    }
}
