// http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
vec3 colorFromKelvin(float temperature) // photographic temperature values are between 15 to 150
{
    vec3 color;
    if(temperature <= 66.0)
    {
        color.r = 1.0;
        color.g = (99.4708025861 * log(temperature) - 161.1195681661) / 255.0;
        if(temperature < 19.0)
        color.b = 0.0;
        else
        color.b = (138.5177312231 * log(temperature - 10.0) - 305.0447927307) / 255.0;
    }
    else
    {
        color.r = (329.698727446 / 255.0) * pow(temperature - 60.0, -0.1332047592);
        color.g = (288.1221695283  / 255.0) * pow(temperature - 60.0, -0.0755148492);
        color.b = 1.0;
    }
    return clamp(color, 0.0, 1.0);
}
