float rgbPuke(vec2 uv, float yaw, float pitch, float time) {
    return sqrt(pow(uv.x + .2, 2) + pow(uv.y - .2, 2));
}
