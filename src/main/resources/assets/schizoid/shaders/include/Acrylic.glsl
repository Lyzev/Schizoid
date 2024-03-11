float luminance(const vec3 rgb) {
    const vec3 w = vec3(.2126, .7152, .0722);
    return dot(rgb, w);
}

vec3 luminosity(const vec3 rgb, const float luminosity) {
    return mix(vec3(luminance(rgb)), rgb, luminosity);
}

float rand(const vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 noiseTile(const vec2 uv, const float scale) {
    return vec3(rand(floor(uv * scale)));
}

vec3 hsb2rgb(const vec3 hsb) {
    vec3 rgb = clamp(abs(mod(hsb.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    rgb = rgb * rgb * (3.0 - 2.0 * rgb);
    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}
