/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

// Special thanks to https://github.com/Sumandora for the help

#version 430 core

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

#define SIMULATION_SPEED .00001
#define AIR_RESISTANCE .98
#define GRAVITY 75000
#define PI 3.14159265359
#define MAX_SPEED 10000
#define MIN_SPEED 1000

#include "Noise.glsl"

void main() {
    Particle p = particles[gl_LocalInvocationIndex + arrayOffset];
    float time = deltaTime * SIMULATION_SPEED;
    p.motion *= AIR_RESISTANCE;
    if (force != 0) {
        vec2 dist = normalize(mousePos - p.position);
        float speed = min(MAX_SPEED, MIN_SPEED + length(GRAVITY * GRAVITY * dist));
        float phi = atan(dist.y, dist.x);
        phi += (rand(p.position) - .5) * (PI / 4); // -22.5 to 22.5 degrees
        vec2 dir = vec2(cos(phi), sin(phi));
        p.motion += dir * speed * force * time;
    }
    vec2 pre = p.position;
    p.position += p.motion;
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
