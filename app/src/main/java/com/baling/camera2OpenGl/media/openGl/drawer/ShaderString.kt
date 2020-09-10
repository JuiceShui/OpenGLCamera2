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
                "attribute float aAlpha;" +
                "varying float vAlpha;" +
                "uniform mat4 uMatrix;" +
                "void main(){" +
                "gl_Position=uMatrix*aPosition;" +
                "vCoordinate=aCoordinate;" +
                "vAlpha=aAlpha;" +
                "}"
        val VIDEO_SHADER_FRAG: String = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "uniform samplerExternalOES uTexture;" +
                "varying float vAlpha;" +
                "void main(){" +
                "vec4 color=texture2D(uTexture,vCoordinate);" +
                "gl_FragColor=vec4(color.r,color.g,color.b,vAlpha);" +
                "}"
        val VIDEO_SOUL_SHADER_FRAG = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "varying float vAlpha;" +
                "uniform samplerExternalOES uTexture;" +
                "uniform float progress;" +
                "uniform int drawFBO;" +
                "uniform sampler2D uSoulTexture;" +
                "void main() {" +
                // 透明度[0,0.4]
                "float alpha = 0.6 * (1.0 - progress);" +
                // 缩放比例[1.0,1.8]
                "float scale = 1.0 + (1.5 - 1.0) * progress;" +

                // 放大纹理坐标
                // 根据放大比例，得到放大纹理坐标 [0,0],[0,1],[1,1],[1,0]
                "float soulX = 0.5 + (vCoordinate.x - 0.5) / scale;\n" +
                "float soulY = 0.5 + (vCoordinate.y - 0.5) / scale;\n" +
                "vec2 soulTextureCoords = vec2(soulX, soulY);" +
                // 获取对应放大纹理坐标下的纹素(颜色值rgba)
                "vec4 soulMask = texture2D(uSoulTexture, soulTextureCoords);" +

                "vec4 color = texture2D(uTexture, vCoordinate);" +

                "if (drawFBO == 0) {" +
                // 颜色混合 默认颜色混合方程式 = mask * (1.0-alpha) + weakMask * alpha
                "    gl_FragColor = color * (1.0 - alpha) + soulMask * alpha;" +
                "} else {" +
                "   gl_FragColor = vec4(color.r, color.g, color.b, vAlpha);" +
                "}" +
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