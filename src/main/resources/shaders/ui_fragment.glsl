#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D textTexture;
uniform vec3 textColor;
uniform float useTexture;

void main() {
    if (useTexture > 0.5) {
        vec4 sampled = vec4(1.0, 1.0, 1.0, texture(textTexture, TexCoord).r);
        FragColor = vec4(textColor, 1.0) * sampled;
    } else {
        FragColor = vec4(textColor, 1.0);
    }
}