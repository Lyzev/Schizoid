// Define sqrt(2*pi) constant for Gaussian normal distribution (we do this to avoid calculating it every time)
#define SQRT_PI_2 2.506628

// Function to compute the Gaussian normal distribution
float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma)) / (SQRT_PI_2 * sigma);
}
