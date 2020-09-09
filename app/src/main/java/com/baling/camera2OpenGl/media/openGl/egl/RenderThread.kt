package com.baling.camera2OpenGl.media.openGl.egl

import android.opengl.GLES20
import android.util.Size
import android.view.Surface
import com.baling.camera2OpenGl.media.openGl.OpenGLTools
import com.baling.camera2OpenGl.media.openGl.drawer.IDrawer
import com.baling.camera2OpenGl.media.openGl.drawer.VideoDrawer

class RenderThread : Thread() {
    //渲染状态
    private var mState = RenderState.NO_SURFACE

    //渲染的view
    private var mSurface: Surface? = null

    //线程锁
    private val mWaitLock = Object()

    //渲染器
    private var mDrawers = mutableListOf<IDrawer>()
    private var mWidth = 0
    private var mHeight = 0
    private var mEGLSurfaceHolder: EGLSurfaceHolder? = null
    private var mHasBindEGLContext = false //是否已经绑定过EGLContext
    private var mHasCreateEGLContext = false//是否已经创建了EGLContext

    //-------线程的等待及解锁

    fun holdOn() {
        synchronized(mWaitLock) {
            mWaitLock.wait()
        }
    }

    fun notifyGo() {
        synchronized(mWaitLock) {
            mWaitLock.notify()
        }
    }

    //---------surface的生命周期
    fun onSurfaceCreate(surface: Surface) {
        mSurface = surface
        mState == RenderState.FRESH_SURFACE
        notifyGo()
    }

    fun onSurfaceSizeChange(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        mState = RenderState.SURFACE_CHANGE
        notifyGo()
    }

    fun onSurfaceDestroy() {
        mState = RenderState.SURFACE_DESTROY
        notifyGo()
    }

    fun onSurfaceStop() {
        mState = RenderState.STOP
        notifyGo()
    }

    override fun run() {
        initEGL()
        while (true) {
            when (mState) {
                RenderState.FRESH_SURFACE -> {
                    createBindEGL()
                    holdOn()
                }
                RenderState.SURFACE_CHANGE -> {
                    createBindEGL()
                    GLES20.glViewport(0, 0, mWidth, mHeight)
                    mDrawers.forEach {
                        it.setScreenSize(Size(mWidth, mHeight))
                    }
                    mState = RenderState.RENDERING
                }
                RenderState.RENDERING -> {
                    render()
                }
                RenderState.SURFACE_DESTROY -> {
                    destroyEGLSurface()
                    mState = RenderState.NO_SURFACE
                }
                RenderState.STOP -> {
                    releaseEGL()
                    return
                }
                else -> {
                    holdOn()
                }
            }
            sleep(20)
        }

    }

    //------------添加渲染器
    fun addDrawer(drawer: IDrawer) {
        mDrawers.add(drawer)
    }

    //-------------EGL相关操作
    fun initEGL() {
        mEGLSurfaceHolder = EGLSurfaceHolder()
        mEGLSurfaceHolder?.init(null, EGL_RECORDABLE_ANDROID)
    }

    /**
     * 创建绑定EGLContext
     */
    fun createBindEGL() {
        if (!mHasBindEGLContext) {
            mEGLSurfaceHolder?.createEGLSurface(mSurface, mWidth, mHeight)
            mEGLSurfaceHolder?.makeCurrent()
            mHasBindEGLContext = true
            if (!mHasCreateEGLContext) {
                mHasCreateEGLContext = true
                generateTextureID()
            }
        }
    }

    fun destroyEGLSurface() {
        mEGLSurfaceHolder?.destroyEGLSurface()
        mHasBindEGLContext = false
    }

    fun releaseEGL() {
        mEGLSurfaceHolder?.release()
    }

    /**
     * 生成并绑定纹理id
     */
    fun generateTextureID() {
        val textureIds = OpenGLTools.genTexture(mDrawers.size)
        for ((id, drawer) in mDrawers.withIndex()) {
            drawer.setTextureID(textureIds[id])
        }
    }

    fun render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        mDrawers.forEach {
            it.draw()
        }
        mEGLSurfaceHolder?.swapBuffers()
    }
}