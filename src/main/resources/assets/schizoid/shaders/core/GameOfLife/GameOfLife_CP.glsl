/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

#version 430 core

precision lowp float;

layout (local_size_x = %d, local_size_y = %d, local_size_z = %d) in;

layout(rgba8, binding = 0) uniform image2D Img0;
layout(rgba8, binding = 1) uniform image2D Img1;

const ivec2 neighborOffsets[] = {
    ivec2(-1), ivec2(0, -1), ivec2(1, -1),
    ivec2(-1, 0), ivec2(1, 0),
    ivec2(-1, 1), ivec2(0, 1), ivec2(1)
};

const int b[] = {%s};
const int s[] = {%s};

void main() {
    int y = int(gl_GlobalInvocationID.x);
    vec2 screenSize = imageSize(Img0);
    if (y >= (screenSize.y)) return;
    for (int x = 0; x < screenSize.x; x++) {
        int aliveNeighbours = 0;
        for (int i = 0; i < 8; i++) {
            ivec2 offset = neighborOffsets[i];
            if (imageLoad(Img0, ivec2(clamp(x + offset.x, 0, screenSize.x - 1), clamp(y + offset.y, 0, screenSize.y - 1))).r > 0.5) {
                aliveNeighbours++;
            }
        }
        if (imageLoad(Img0, ivec2(x, y)).r > 0.5) {
            for (int i = 0; i < s.length(); i++) {
                if (aliveNeighbours == s[i]) {
                    imageStore(Img1, ivec2(x, y), vec4(1));
                    break;
                }
            }
        } else {
            for (int i = 0; i < b.length(); i++) {
                if (aliveNeighbours == b[i]) {
                    imageStore(Img1, ivec2(x, y), vec4(1));
                    break;
                }
            }
        }
    }
}
