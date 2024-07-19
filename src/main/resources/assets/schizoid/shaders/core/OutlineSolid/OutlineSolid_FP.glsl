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

#define BASE_EDGE_THICKNESS 1.8 // Base edge thickness

// Function to compute squared distance between two points
float distSquared(vec2 A, vec2 B) {
    vec2 C = A - B;
    return dot(C, C);
}

void main() {
    // Initialize the output color to transparent black
    color = vec4(0.0);

    // Sample the texture at the current UV coordinates
    vec4 col = texture(Tex0, uv);

    // Check if the sampled alpha value is greater than 0.0
    if (col.a > 0.0) {
        // Compute the squared distance between the current point and the texture coordinate stored in col.xy
        float distance = distSquared(uv * ScreenSize, col.xy * ScreenSize);

        // Compute the squared length of the outline
        float lengthSquared = Length * Length;

        // Adjust the edge thickness based on Length
        float edgeThickness = BASE_EDGE_THICKNESS * Length;

        // Apply a smooth step function to create anti-aliasing effect
        float alpha = smoothstep(lengthSquared - edgeThickness, lengthSquared + edgeThickness, distance);

        // Set the output color with smooth alpha transition
        color = vec4(1.0, 1.0, 1.0, 1.0 - alpha);
    }
}
