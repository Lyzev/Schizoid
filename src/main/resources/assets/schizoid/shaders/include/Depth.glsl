#define linearizeDepth(depthValue, near, far) (2.0 * near * far) / (far + near - depthValue * (far - near))
