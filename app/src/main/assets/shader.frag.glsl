#extension GL_OES_EGL_image_external : require
precision highp float;

varying vec2 vPreviewCoord;
uniform samplerExternalOES texPreview;
uniform mat4 matColorMatrix;
void main() {
    gl_FragColor=matColorMatrix*texture2D(texPreview, vPreviewCoord).rgba;
}
