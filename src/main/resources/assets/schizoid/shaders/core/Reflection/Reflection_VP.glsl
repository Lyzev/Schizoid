/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 330

layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 normal;

out vec3 Normal;
out vec3 FragPos;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

void main()
{
    FragPos = vec3(ModelViewMat * vec4(pos, 1.0));
    Normal = mat3(transpose(inverse(ModelViewMat))) * normal;

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
