#version 330 core

#define BITMASK_8 255u
#define BITMASK_4 15u
#define BITMASK_3 7u
#define BITMASK_10 1023u
#define BITMASK_2 3u
#define CHUNK_SIZE 32
#define SHADOWS float[6](1.2, 0.3, 0.4, 0.6, 0.8, 1.0)// Array of shadow levels

#define NOISE_SCALE 1000.0// Scale for noise input
#define NOISE_SPEED 5000.0// Speed of noise change over time
#define NOISE_AMPLITUDE 16.0
// Amplitude of the noise effect

layout(location = 0) in uint data1;
layout(location = 1) in uint data2;

out vec2 TexCoord;
out float shadow;
out float waveHeight;
out float fresnel;
out vec3 pos;

uniform float time;
uniform mat4 view;
uniform mat4 projection;

uniform vec3 cameraPosition;
uniform ivec3 position;

// Function to generate simple pseudo-random noise value
float simpleNoise(float x, float z) {
    float n = dot(vec2(x, z), vec2(127.1, 311.7));// Create a seed value
    return cos(sin(n) * 43758.5453);// Fractal noise using sine function
}

void main() {
    // Unpack data1
    float x = float((data1 >> 22) & BITMASK_10);
    float y = float((data1 >> 12) & BITMASK_10);
    float z = float((data1 >> 2) & BITMASK_10);
    uint pointType = data1 & BITMASK_2;

    x = round(x / 2 * CHUNK_SIZE) / CHUNK_SIZE;
    y = round(y / 2 * CHUNK_SIZE) / CHUNK_SIZE;
    z = round(z / 2 * CHUNK_SIZE) / CHUNK_SIZE;

    // Unpack data2
    float r = float((data2 >> 28) & BITMASK_4) / float(BITMASK_4);
    float g = float((data2 >> 24) & BITMASK_4) / float(BITMASK_4);
    float b = float((data2 >> 20) & BITMASK_4) / float(BITMASK_4);

    uint normal = (data2 >> 17) & BITMASK_3;

    float u = float((data2 >> 9) & BITMASK_8);
    float v = float((data2 >> 1) & BITMASK_8);
    TexCoord = vec2(u, v);

    vec3 xyz = vec3(x, y, z)+position*CHUNK_SIZE;

    vec3 toCameraVector = cameraPosition - xyz;
    vec3 viewVector = normalize(toCameraVector);
    vec3 faceNormal = vec3(0.0, 1.0, 0.0);
    fresnel = abs(dot(viewVector, faceNormal));

    // Shadow calculation
    shadow = (normal < 6u) ? SHADOWS[normal] : 1.0;
    pos = xyz;

    // Calculate position and set vertex position
    vec4 position = vec4(xyz, 1.0);
    gl_Position = projection * view * position;
}