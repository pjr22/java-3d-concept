#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec2 aTexCoord;

out vec3 fragColor;
out vec3 fragNormal;
out vec3 fragPos;
out vec2 fragTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    fragPos = vec3(model * vec4(aPos, 1.0));
    fragNormal = mat3(transpose(inverse(model))) * aNormal;
    fragColor = aColor;
    fragTexCoord = aTexCoord;
    
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}
