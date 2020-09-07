package com.baling.camera2OpenGl.media.openGl.drawer

class ShaderString {
    companion object {
        val TRIANGLE_SHADER_VERTEX: String = "attribute vec4 aPosition;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "}"

        val TRIANGLE_SHADER_FRAG: String = "precision mediump float;" +
                "void main(){" +
                "gl_FragColor=vec4(1.0,0.5,0.4,1.0);" +
                "}"

        val BITMAP_SHADER_VERTEX: String = "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "}"
        val BITMAP_SHADER_FRAG: String = "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  gl_FragColor = color;" +
                "}"
        val VIDEO_SHADER_VERTEX: String = "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "uniform mat4 uMatrix;" +
                "void main(){" +
                "gl_Position=aPosition*uMatrix;" +
                "vCoordinate=aCoordinate;" +
                "}"
        val VIDEO_SHADER_FRAG: String = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "void main(){" +
                "vec4 color=texture2D(uTexture,vCoordinate);" +
                "gl_FragColor=color;" +
                "}"

        //黑白画面
        val VIDEO_SHADER_FRAG_GRAY: String = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "void main(){" +
                "vec4 color=texture2D(uTexture,vCoordinate);" +
                "float gray=(color.r+color.g+color.b)/3.0;" +
                "gl_FragColor=vec4(gray,gray,gray,1.0);" +
                "}"
    }
}