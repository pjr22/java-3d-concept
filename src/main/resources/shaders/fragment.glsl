#version 330 core

in vec3 fragColor;
in vec3 fragNormal;
in vec3 fragPos;

out vec4 FragColor;

uniform vec3 objectColor;
uniform vec3 lightDirection;
uniform float ambientStrength;

void main() {
    vec3 norm = normalize(fragNormal);
    vec3 lightDir = normalize(-lightDirection);
    
    float diff = max(dot(norm, lightDir), 0.0);
    
    vec3 ambient = ambientStrength * objectColor;
    vec3 diffuse = diff * objectColor;
    
    vec3 result = ambient + diffuse;
    FragColor = vec4(result, 1.0);
}
