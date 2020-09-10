package com.baling.camera2OpenGl.media.openGl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import com.baling.camera2OpenGl.media.openGl.drawer.IDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender : GLSurfaceView.Renderer {
    private val mDrawers = mutableListOf<IDrawer>()
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND)
        // FIXME: 2020/9/8
        //配置混合算法  TODO GL_SRC_ALPHA会黑屏？？？为什么  问题解决！
        // ----->>>glEnableVertexAttribArray只用于启动顶点，不是用来启动所有句柄的
        // ，但为什么启动其他会导致异常暂时不知但问题造成的已知
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val textureId = OpenGLTools.genTexture(mDrawers.size)
        for ((id, drawer) in mDrawers.withIndex()) {
            drawer.setTextureID(textureId[id])
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        for ((id, drawer) in mDrawers.withIndex()) {
            drawer.setScreenSize(Size(width, height))
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(
            GLES20.GL_COLOR_BUFFER_BIT
                    or GLES20.GL_DEPTH_BUFFER_BIT
        )
        mDrawers.forEach {
            it.draw()
        }
    }

    fun addDrawer(drawer: IDrawer) {
        mDrawers.add(drawer)
    }
}