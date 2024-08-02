/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

in vec2 uv;
out vec4 color;

uniform sampler2D Tex0;
uniform sampler2D Tex1;

void main()
{
    vec4 sceneColor = texture(Tex0, uv);
    vec4 fboColor = texture(Tex1, uv);
    color = clamp(sceneColor + fboColor, 0.0, 1.0);
}
