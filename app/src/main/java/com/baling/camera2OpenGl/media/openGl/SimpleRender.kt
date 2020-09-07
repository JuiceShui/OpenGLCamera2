package com.baling.camera2OpenGl.media.openGl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import com.baling.camera2OpenGl.media.openGl.drawer.IDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender : GLSurfaceView.Renderer {
    private val mDrawers = mutableListOf<IDrawer>()
    override fun onDrawFrame(gl: GL10?) {
        mDrawers.forEach {
            it.draw()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        for ((id, drawer) in mDrawers.withIndex()) {
            drawer.setScreenSize(Size(width, height))
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        val textureId = OpenGLTools.genTexture(mDrawers.size)
        for ((id, drawer) in mDrawers.withIndex()) {
            drawer.setTextureID(textureId[id])
        }
    }

    fun addDrawer(drawer: IDrawer) {
        mDrawers.add(drawer)
    }
}