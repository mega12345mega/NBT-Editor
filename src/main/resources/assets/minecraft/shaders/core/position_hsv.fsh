#version 150

in vec2 fragPos;
in float hue;

out vec4 fragColor;

// From https://gist.github.com/983/e170a24ae8eba2cd174f
// Which seems to have it from http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 color = vec4(hsv2rgb(vec3(hue, fragPos.x, 1 - fragPos.y)), 1);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
