package com.baling.camera2OpenGl.media.openGl.egl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface
import javax.microedition.khronos.egl.EGL
import kotlin.math.E

const val FLAG_RECORDABLE = 0x01
const val EGL_RECORDABLE_ANDROID = 0x3142

class EGLCore {
    private val TAG = "EGLCore"

    //EGL相关变量
    private var mEGLDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGLConfig: EGLConfig? = null
    fun init(eglContext: EGLContext?, flags: Int) {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "EGL already set up！！")
            return
        }
        val shaderContext = eglContext ?: EGL14.EGL_NO_CONTEXT
        //获取eglDisplay
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "unable to get EGL14 display")
            return
        }

        //初始化EGL
        val version = IntArray(2)
        val success = EGL14.eglInitialize(
            mEGLDisplay, version, 0, version, 1
        )
        if (!success) {
            mEGLDisplay = EGL14.EGL_NO_DISPLAY
            Log.e(TAG, "unable to init egl14")
            return
        }

        //初始化EGLConfig ,EGLContext上下文
        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            val config = getEGLConfig(flags, 2) ?: return //获取config失败，返回
            val attrList = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            val contxt = EGL14.eglCreateContext(
                mEGLDisplay, config,
                shaderContext, attrList, 0
            )
            mEGLConfig = config
            mEGLContext = contxt
        }

    }

    fun getEGLConfig(flags: Int, version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        if (version > 3) {
            //配置EGL3
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }
        //配置数组，主要是配置RGBA和深度位数 两个为一对，
        // 前面是key，后面是value，数组以EGL_NO_DISPLAY结尾
        val attrList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,
            EGL14.EGL_NONE
        )
        //配置android的指定标记
        if (flags and FLAG_RECORDABLE != 0) {
            attrList[attrList.size - 3] = EGL_RECORDABLE_ANDROID
            attrList[attrList.size - 2] = 1
        }
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        //获取可用的EGL配置列表
        val success = EGL14.eglChooseConfig(
            mEGLDisplay, attrList, 0, configs,
            0, configs.size, numConfigs, 0
        )
        if (!success) {
            Log.e(TAG, "unable to find RGBA888 $version EGLConfig")
            return null
        }
        //使用系统推荐的第一个配置
        return configs[0]
    }

    /**
     * 创建可显示的渲染缓存
     * @param surface 渲染窗口的surface
     */

    fun createWindowSurface(surface: Any): EGLSurface {
        if (surface !is Surface && surface !is SurfaceTexture) {
            Log.e(TAG, "invalid surface:$surface")
        }
        val surfaceAttr = intArrayOf(EGL14.EGL_NONE)
        val eglSurface =
            EGL14.eglCreateWindowSurface(
                mEGLDisplay, mEGLConfig,
                surface, surfaceAttr, 0
            )
        if (eglSurface == null) {
            Log.e(TAG, "surface was null")
        }
        return eglSurface
    }

    /**
     * 创建离屏渲染缓存
     * @param width 缓存的窗口宽度
     * @param height 缓存的窗口高度
     */
    fun createOffScreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttr = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )
        val eglSurface = EGL14.eglCreatePbufferSurface(
            mEGLDisplay, mEGLConfig, surfaceAttr, 0
        )
        if (eglSurface == null) {
            Log.e(TAG, "surface was null")
        }
        return eglSurface
    }

    /**
     * 将当前线程与上下文进行绑定
     */
    fun makeCurrent(eglSurface: EGLSurface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "EGLDisplay is null ,call init first")
            return
        }
        val success = EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)
        if (!success) {
            Log.e(TAG, "EGLMakeCurrent(eglSurface) failed")
        }
    }

    /**
     * 将当前线程与上下文进行绑定
     */
    fun makeCurrent(drawSurface: EGLSurface, readSurface: EGLSurface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, "EGLDisplay is null ,call init first")
        }
        val success = EGL14.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)
        if (!success) {
            Log.e(TAG, "EGLMakeCurrent($drawSurface,$readSurface) failed")
        }
    }

    /**
     * 将缓存图像数据发送到display进行渲染
     */
    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    /**
     * 设置当前帧的时间  单位 纳秒
     */
    fun setPresentationTime(eglSurface: EGLSurface, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    /**
     * 摧毁EGLSurface 并解绑上下文
     */
    fun destroySurface(eglSurface: EGLSurface) {
        EGL14.eglMakeCurrent(
            mEGLDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface)
    }

    /**
     * 释放资源
     */
    fun release() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig = null
    }
}