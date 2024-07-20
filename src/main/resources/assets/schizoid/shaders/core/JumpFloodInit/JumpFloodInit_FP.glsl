/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

precision highp float;

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;

/*
 * This is the initialization fragment shader for the Jump Flooding algorithm.
 */
void main() {
    // Reads the color from the texture at the given UV coordinates.
    // Checks the alpha component of the color.
    if (texture(Tex0, uv).a > 0.0) {
        // For fragments with alpha > 0.0, marks the position with UV coordinates and makes it fully opaque.
        color = vec4(uv, 0.0, 1.0);
    } else {
        // For fragments with alpha <= 0.0, sets the output color to transparent black.
        color = vec4(0.0);
    }
}
