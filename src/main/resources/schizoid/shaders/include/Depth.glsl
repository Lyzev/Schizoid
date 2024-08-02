#define linearizeDepth(depth, near, far) (2.0 * near * far) / (far + near - depth * (far - near))
