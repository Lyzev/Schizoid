float luminance(const vec3 rgb) {
    const vec3 w = vec3(0.2126, 0.7152, 0.0722);
    return dot(rgb, w);
}

vec3 luminosity(const vec3 rgb, const float luminosity) {
    return mix(vec3(luminance(rgb)), rgb, luminosity);
}
