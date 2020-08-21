package com.baling.camera2OpenGl.shader

class ReverseShader : NormalShader() {
    private val DECOLOR_MATRIX = floatArrayOf(
        -1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, -1.0f, 0.0f, 1.0f,
        0.0f, 0.0f, -1.0f, 1.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    override fun getEffectMatrix(): FloatArray {
        return DECOLOR_MATRIX
    }
}