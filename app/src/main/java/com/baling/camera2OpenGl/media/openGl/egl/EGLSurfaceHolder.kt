package com.baling.camera2OpenGl.media.openGl.egl

import android.opengl.EGLContext
import android.opengl.EGLSurface

class EGLSurfaceHolder {
    private val TAG = "EGLSurfaceHolder"
    lateinit var mEGLCore: EGLCore
    var mEGLSurface: EGLSurface? = null

    fun init(shareContext: EGLContext?, flags: Int) {
        mEGLCore = EGLCore()
        mEGLCore.init(shareContext, flags)
    }

    fun createEGLSurface(surface: Any?, width: Int = -1, height: Int = -1) {
        mEGLSurface = if (surface != null) {
            mEGLCore.createWindowSurface(surface)
        } else {
            mEGLCore.createOffScreenSurface(width, height)
        }
    }

    fun makeCurrent() {
        if (mEGLSurface != null) {
            mEGLCore.makeCurrent(mEGLSurface!!)
        }
    }

    fun swapBuffers() {
        if (mEGLSurface != null) {
            mEGLCore.swapBuffers(mEGLSurface!!)
        }
    }

    fun destroyEGLSurface() {
        if (mEGLSurface != null) {
            mEGLCore.destroySurface(mEGLSurface!!)
            mEGLSurface = null
        }
    }

    fun release() {
        mEGLCore.release()
    }
}