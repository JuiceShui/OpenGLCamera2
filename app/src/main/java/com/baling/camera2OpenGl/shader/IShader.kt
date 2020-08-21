package com.baling.camera2OpenGl.shader

import android.content.Context
import com.baling.camera2OpenGl.EGLHelper

interface IShader {
    fun onAttach(ctx: Context, helper: EGLHelper)
    fun onDetach()
    fun draw(helper: EGLHelper, transformMatrix: FloatArray, oesTexture: Int)
}