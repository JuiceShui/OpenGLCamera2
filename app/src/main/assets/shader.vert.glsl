attribute vec2 vPosition;
attribute vec4 vCoord;

varying vec4 vPreviewCoord;
uniform mat4 matTransform;

void main(){
    gl_Position=vec4(vPosition, 0, 1);
    mPreviewCoord=(matTransform*vCoord).xy;
}