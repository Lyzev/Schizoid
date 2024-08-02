float rand(const vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 noiseTile(const vec2 uv, const float scale) {
    return vec3(rand(uv * scale));
}
