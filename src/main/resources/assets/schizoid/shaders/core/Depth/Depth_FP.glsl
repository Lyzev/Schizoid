/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

#include "Depth.glsl"

in vec2 uv;
out vec4 color;

uniform sampler2D uScene;
uniform sampler2D uDepth;
uniform float uNear;
uniform float uFar;
uniform float uMinThreshold;
uniform float uMaxThreshold;

void main() {
    // Read in depth value from depth texture
    float depthValue = texture(uDepth, uv).x;

    // Convert depth value to distance
    float distance = linearizeDepth(depthValue, uNear, uFar) / uFar;

    color = vec4(0);
    if (distance > uMinThreshold) {
        color = vec4(texture(uScene, uv).rgb, min((distance - uMinThreshold) / (uMaxThreshold - uMinThreshold), 1.0));
    }
}
