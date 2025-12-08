#version 330 core

in vec3 fragColor;
in vec3 fragNormal;
in vec3 fragPos;
in vec2 fragTexCoord;

out vec4 FragColor;

uniform vec3 objectColor;
uniform vec3 lightDirection;
uniform float ambientStrength;
uniform sampler2D textureSampler;
uniform bool useTexture;

void main() {
    vec3 norm = normalize(fragNormal);
    vec3 lightDir = normalize(-lightDirection);
    
    float diff = max(dot(norm, lightDir), 0.0);
    
    // Determine the base color (texture or object color)
    vec3 baseColor;
    if (useTexture) {
        vec4 texColor = texture(textureSampler, fragTexCoord);
        baseColor = mix(objectColor, texColor.rgb, texColor.a);
    } else {
        baseColor = objectColor;
    }
    
    vec3 ambient = ambientStrength * baseColor;
    vec3 diffuse = diff * baseColor;
    
    vec3 result = ambient + diffuse;
    FragColor = vec4(result, 1.0);
}
