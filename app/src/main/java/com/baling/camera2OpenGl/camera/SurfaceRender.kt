package com.baling.camera2OpenGl.camera

import android.content.Context
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.Surface
import com.baling.camera2OpenGl.camera.shader.IShader

class SurfaceRender(helper: EGLHelper, surface: Surface, width: Int, height: Int) {
    var mEGLHelper: EGLHelper = helper
    lateinit var mEGLSurface: EGLSurface
    var mShader: IShader? = null
    var mWidth: Int = width
    var mHeight: Int = height

    init {
        mEGLSurface = mEGLHelper.createEGLSurface(surface)
    }

    fun setShader(context: Context, shader: IShader) {
        if (mShader != null) {
            mShader?.onDetach()
        }
        mShader = shader
        mEGLHelper.makeCurrent(mEGLSurface)
        mShader!!.onAttach(context, mEGLHelper)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glViewport(0, 0, mWidth, mHeight)
    }

    fun render(transformMatrix: FloatArray) {
        render(transformMatrix, mEGLSurface)
    }

    fun render(transformMatrix: FloatArray, eglSurface: EGLSurface) {
        mEGLHelper.makeCurrent(eglSurface)
        mShader!!.draw(mEGLHelper, transformMatrix, mEGLHelper.getTexture())
        mEGLHelper.swapBuffers(eglSurface)
    }
}