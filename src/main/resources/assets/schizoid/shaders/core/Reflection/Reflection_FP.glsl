/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec3 Normal;
in vec3 FragPos;
out vec4 Color;

uniform sampler2D Tex0;
uniform float Freq;
uniform vec3 CamPos;

#define RECIPROCAL_PI2 .15915494

#include "SimplexNoise.glsl"

// Credit to https://en.wikipedia.org/wiki/Cube_mapping
void main() {
    vec3 normal = normalize(Normal - CamPos);
    vec3 I = normalize(FragPos + snoise(FragPos.yz) * Freq);
    vec3 R = reflect(I, normal + snoise(FragPos.yz) * Freq);
    vec3 cubeDirection = normalize(R);

    // -- CUBE MAPPING
    float x = cubeDirection.x;
    float y = cubeDirection.y;
    float z = cubeDirection.z;

    float absX = abs(x);
    float absY = abs(y);
    float absZ = abs(z);

    bool isXPositive = x > 0;
    bool isYPositive = y > 0;
    bool isZPositive = z > 0;

    float maxAxis, uc, vc;

    int index;
    float u, v;

    // POSITIVE X
    if (isXPositive && absX >= absY && absX >= absZ) {
        // u (0 to 1) goes from +z to -z
        // v (0 to 1) goes from -y to +y
        maxAxis = absX;
        uc = -z;
        vc = y;
        index = 0;
    }
    // NEGATIVE X
    if (!isXPositive && absX >= absY && absX >= absZ) {
        // u (0 to 1) goes from -z to +z
        // v (0 to 1) goes from -y to +y
        maxAxis = absX;
        uc = z;
        vc = y;
        index = 1;
    }
    // POSITIVE Y
    if (isYPositive && absY >= absX && absY >= absZ) {
        // u (0 to 1) goes from -x to +x
        // v (0 to 1) goes from +z to -z
        maxAxis = absY;
        uc = x;
        vc = -z;
        index = 2;
    }
    // NEGATIVE Y
    if (!isYPositive && absY >= absX && absY >= absZ) {
        // u (0 to 1) goes from -x to +x
        // v (0 to 1) goes from -z to +z
        maxAxis = absY;
        uc = x;
        vc = z;
        index = 3;
    }
    // POSITIVE Z
    if (isZPositive && absZ >= absX && absZ >= absY) {
        // u (0 to 1) goes from -x to +x
        // v (0 to 1) goes from -y to +y
        maxAxis = absZ;
        uc = x;
        vc = y;
        index = 4;
    }
    // NEGATIVE Z
    if (!isZPositive && absZ >= absX && absZ >= absY) {
        // u (0 to 1) goes from +x to -x
        // v (0 to 1) goes from -y to +y
        maxAxis = absZ;
        uc = -x;
        vc = y;
        index = 5;
    }

    // Convert range from -1 to 1 to 0 to 1
    u = .5 * (uc / maxAxis + 1);
    v = .5 * (vc / maxAxis + 1);

    vec3 reflection = texture(Tex0, vec2(u, v)).rgb;
    Color = vec4(reflection, 1);
}
