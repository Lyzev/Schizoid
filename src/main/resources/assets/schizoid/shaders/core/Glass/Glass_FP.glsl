/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec3 Normal;
in vec3 FragPos;
out vec4 color;

uniform samplerCube cubeMap;
uniform vec3 CameraPosition;

void main() {
    vec3 normal = normalize(Normal - CameraPosition);
    vec3 I = normalize(FragPos + 3); // random value to make the improve the glass effect
    vec3 R = reflect(I, normal);
    vec3 reflection = texture(cubeMap, R - .2).rgb; // random value to make the improve the glass effect
    color = vec4(reflection, 1);
}
