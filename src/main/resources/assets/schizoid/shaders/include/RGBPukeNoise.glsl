float rgbPuke(vec2 uv, float yaw, float pitch, float time) {
    vec2 pos = vec2(uv.x + yaw / 180.0, uv.y - pitch / 90.0);
    return snoise(vec3(pos, time));
}
