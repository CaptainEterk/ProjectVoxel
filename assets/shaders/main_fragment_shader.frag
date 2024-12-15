#version 330 core

#define MAX_UV_SIZE 16u
#define BITMASK_8 255u
#define BITMASK_4 15u
#define BITMASK_3 7u
#define CLOSE_FRESNEL_COLOR vec4(0.0, 0.5, 0.5, 0.4)
#define FAR_FRESNEL_COLOR vec4(0.0, 0.25, 0.3, 0.95)

in vec2 TexCoord;
in float shadow;
in float waveHeight;
in float fresnel;
in vec3 pos;

out vec4 FragColor;

uniform sampler2D texture1;
uniform vec3 cameraPosition;

uniform vec4 fogColor;
uniform float farFogDistance;
uniform float nearFogDistance;

uniform vec4 filterColor;
uniform float time;

void main() {
    // Sample the texture at the pixelated texture coordinate
    vec4 color = texture(texture1, TexCoord / MAX_UV_SIZE);
    if (color.a == 0) discard;

    // Apply shadow effect
    FragColor = vec4(color.rgb * shadow, color.a);

    // Fresnel effect (uncomment to enable)
    // if (waveHeight != -100) {
    //     FragColor *= vec4(vec3((waveHeight + 1) / 2), 1 - fresnel);
    //     FragColor += CLOSE_FRESNEL_COLOR * fresnel + FAR_FRESNEL_COLOR * (1 - fresnel);
    // }

    // Add fog (uncomment to enable)
    // float dist = length(pos - cameraPosition);
    // float fog_factor = (farFogDistance - dist) / (farFogDistance - nearFogDistance);
    // fog_factor = clamp(fog_factor, 0.0, 1.0);
    // FragColor = mix(fogColor, FragColor, fog_factor);

    // Grayscale effect (uncomment to enable)
    // float sum = FragColor.r + FragColor.g + FragColor.b;
    // sum /= 3.0;
    // FragColor = vec4(vec3(sum), 1.0);
}