/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

#include "Depth.glsl"

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform float Near;
uniform float Far;

void main() {
    // Read in depth value from depth texture
    float depthValue = texture(Tex0, uv).x;

    // Convert depth value to distance
    float distance = linearizeDepth(depthValue, Near, Far) / Far;

    color = vec4(vec3(distance), 1);
}
