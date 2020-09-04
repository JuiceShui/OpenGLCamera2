package com.baling.camera2OpenGl.camera.shader

class NormalShader : BasicShader() {
    val DECOLOR_MATRIX = floatArrayOf(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    )

    override fun getEffectMatrix(): FloatArray {
        return DECOLOR_MATRIX
    }
}