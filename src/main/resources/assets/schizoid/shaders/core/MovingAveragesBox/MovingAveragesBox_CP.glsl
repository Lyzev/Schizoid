#version 450

layout(local_size_x = %d, local_size_y = %d, local_size_z = %d ) in;

precision highp float;
precision highp int;

layout(rgba8, binding = 0) uniform image2D Img0;
layout(rgba8, binding = 1) uniform image2D Img1;
uniform mat2 Direction;
uniform bool Alpha;
uniform int Strength;

void main() {
    int cKernelSize = Strength * 2 + 1;
    int cKernelHalfDist = cKernelSize / 2;
    float recKernelSize = 1 / float(cKernelSize);

    int y = int(gl_GlobalInvocationID.x);

    vec2 tex0Size = imageSize(Img1) * Direction;

    vec2 screenSize = imageSize(Img0) * Direction;
    // avoid processing pixels that are out of texture dimensions!
    if (y >= (screenSize.y)) return;

    vec4 colourSum = imageLoad(Img1, ivec2((vec2(0, y) / screenSize) * tex0Size * Direction)) * float(cKernelHalfDist);
    for (int x = 0; x <= cKernelHalfDist; x++) {
        colourSum += imageLoad(Img1, ivec2((vec2(x, y) / screenSize) * tex0Size * Direction));
    }

    for (int x = 0; x < screenSize.x; x++) {
        vec4 color = clamp(colourSum * recKernelSize, 0, 1);
        if (!Alpha) {
            color.a = 1;
        }
        imageStore(Img0, ivec2(vec2(x, y) * Direction), color);

        // move window to the next
        vec4 leftBorder = imageLoad(Img1, ivec2((vec2(max(x-cKernelHalfDist, 0), y) / screenSize) * tex0Size * Direction));
        vec4 rightBorder = imageLoad(Img1, ivec2((vec2(min(x+cKernelHalfDist + 1, screenSize.x - 1), y) / screenSize) * tex0Size * Direction));

        colourSum -= leftBorder;
        colourSum += rightBorder;
    }
}
