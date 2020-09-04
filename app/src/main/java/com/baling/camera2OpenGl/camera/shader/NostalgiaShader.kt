package com.baling.camera2OpenGl.camera.shader

class NostalgiaShader : BasicShader() {
    private val DECOLOR_MATRIX = floatArrayOf(
        0.393f, 0.769f, 0.189f, 0.0f,
        0.349f, 0.686f, 0.168f, 0.0f,
        0.272f, 0.534f, 0.131f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    override fun getEffectMatrix(): FloatArray {
        return DECOLOR_MATRIX
    }
}