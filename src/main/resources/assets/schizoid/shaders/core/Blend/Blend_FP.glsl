/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D scene;
uniform sampler2D textureSampler;

void main()
{
    vec4 sceneColor = texture(scene, uv);
    vec4 fboColor  = texture(textureSampler, uv);
    color = clamp(sceneColor + fboColor, 0.0, 1.0);
}
