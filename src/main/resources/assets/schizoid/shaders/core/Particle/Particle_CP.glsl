/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

// Special thanks to https://github.com/Sumandora for the help

#version 430 core

precision lowp float;

layout (local_size_x = %d, local_size_y = %d, local_size_z = %d) in;

struct Particle {
    vec2 position;
    vec2 motion;
};

layout (std140, binding = 0) buffer ParticleBuffer { Particle particles[]; };
layout(location = 1) uniform vec2 mousePos;
layout(location = 2) uniform vec2 screenSize;
layout(location = 3) uniform float force;
layout(location = 4) uniform int arrayOffset;
layout(location = 5) uniform float deltaTime;
layout(location = 6) uniform vec3 colorIdle;
layout(location = 7) uniform vec3 colorActive;

layout(rgba32f, binding = 0) uniform image2D imgOutput;

#define SIMULATION_SPEED 1 / (1000 / 60) // 60 fps
#define AIR_RESISTANCE .98
#define GRAVITY 8.91
#define PI 3.14159265359
#define MAX_SPEED 8.91

#include "Noise.glsl"

void main() {
    Particle p = particles[gl_LocalInvocationIndex + arrayOffset];
    float simulate = deltaTime * SIMULATION_SPEED;
    p.motion *= AIR_RESISTANCE + .02 * (1 - simulate);
    if (force != 0) {
        vec2 dist = normalize(mousePos - p.position);
        float speed = min(MAX_SPEED, length(GRAVITY * dist));
        float phi = atan(dist.y, dist.x);
        phi += (rand(p.position) - .5) * (PI / 4); // -22.5° to 22.5°
        vec2 dir = vec2(cos(phi), sin(phi));
        p.motion += dir * speed * force * simulate;
    }
    vec2 pre = p.position;
    p.position += p.motion * simulate;
    if (p.position.x < 0 || p.position.x > screenSize.x) {
        p.position.x = pre.x;
        p.motion.x *= -1;
    }
    if (p.position.y < 0 || p.position.y > screenSize.y) {
        p.position.y = pre.y;
        p.motion.y *= -1;
    }
    vec3 color = mix(colorIdle, colorActive, min(length(p.motion) / 2000, 1));
    imageStore(imgOutput, ivec2(p.position), vec4(color, 1));
    particles[gl_LocalInvocationIndex + arrayOffset] = p;
}
