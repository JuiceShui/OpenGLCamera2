package com.baling.camera2OpenGl.camera.shader

class DecolorShader : BasicShader() {
    private val DECOLOR_MATRIX = floatArrayOf(
        0.299f, 0.587f, 0.114f, 0.0f,
        0.299f, 0.587f, 0.114f, 0.0f,
        0.299f, 0.587f, 0.114f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    override fun getEffectMatrix(): FloatArray {
        return DECOLOR_MATRIX
    }
}