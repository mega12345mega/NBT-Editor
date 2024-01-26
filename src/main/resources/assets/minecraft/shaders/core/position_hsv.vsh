#version 150

in vec3 Position;
in vec2 UV0;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 fragPos;
out float hue;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    fragPos = UV0;
    hue = UV2[0] / 360.0f;
}
