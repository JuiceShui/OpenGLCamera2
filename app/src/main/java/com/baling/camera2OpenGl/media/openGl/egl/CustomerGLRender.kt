package com.baling.camera2OpenGl.media.openGl.egl

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.baling.camera2OpenGl.media.openGl.drawer.IDrawer
import java.lang.ref.WeakReference

class CustomerGLRender : SurfaceHolder.Callback {
    private val mThread = RenderThread()
    private var mSurfaceView: WeakReference<SurfaceView>? = null
    private var mSurface: Surface? = null

    init {
        mThread.start()
    }

    fun setSurface(surfaceView: SurfaceView) {
        mSurfaceView = WeakReference(surfaceView)
        mSurfaceView?.get()?.holder?.addCallback(this)
        mSurfaceView?.get()?.addOnAttachStateChangeListener(object :
            View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                mThread.onSurfaceStop()
            }

            override fun onViewAttachedToWindow(v: View?) {
            }

        })
    }

    fun setSurface(surface: Surface, width: Int, height: Int) {
        mSurface = surface
        mThread.onSurfaceCreate(mSurface!!)
        mThread.onSurfaceSizeChange(width, height)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mThread.onSurfaceSizeChange(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mThread.onSurfaceDestroy()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mThread.onSurfaceCreate(holder!!.surface)
    }

    fun addDrawers(drawer: IDrawer) {
        mThread.addDrawer(drawer)
    }
}