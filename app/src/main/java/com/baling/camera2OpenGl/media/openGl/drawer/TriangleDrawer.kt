package com.baling.camera2OpenGl.media.openGl.drawer

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.Size
import com.baling.camera2OpenGl.media.openGl.OpenGLTools
import java.nio.FloatBuffer

class TriangleDrawer : IDrawer {
    private val VERTEX = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        0f, 1f
    )
    private val TEXTURE_COORD = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0.5f, 0f
    )
    private val mOrders = shortArrayOf(
        0, 1, 2
    )
    private var mVertexPosHandler: Int = -1
    private var mProgram: Int = -1
    private var mVertexBuffer: FloatBuffer

    init {
        mVertexBuffer = OpenGLTools.getFloatBuffer(VERTEX)
    }

    override fun draw() {
        createGLProgram()
        onDraw()
    }

    override fun setTextureID(id: Int) {
    }

    override fun release() {
    }

    override fun setVideoSize(size: Size) {
    }

    override fun setScreenSize(size: Size) {
    }

    override fun setAlpha(alpha: Float) {
    }

    override fun getSurfaceTexture(): SurfaceTexture? {
        return null
    }

    fun createGLProgram() {
        if (mProgram == -1) {
            mProgram = GLES20.glCreateProgram()
            val shaderVertex =
                OpenGLTools.loadShader(GLES20.GL_VERTEX_SHADER, ShaderString.TRIANGLE_SHADER_VERTEX)
            val shaderFrag =
                OpenGLTools.loadShader(GLES20.GL_FRAGMENT_SHADER, ShaderString.TRIANGLE_SHADER_FRAG)
            GLES20.glAttachShader(mProgram, shaderVertex)
            GLES20.glAttachShader(mProgram, shaderFrag)
            GLES20.glLinkProgram(mProgram)
            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
        }
        GLES20.glUseProgram(mProgram)
    }

    fun onDraw() {
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        //设置着色器参数，第二个参数代表一个顶点包含的数据量，这里只有xy，所以是2
        GLES20.glVertexAttribPointer(
            mVertexPosHandler, 2, GLES20.GL_FLOAT,
            false, 0, mVertexBuffer
        )
        //按order的顺序draw
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mOrders.size,
            GLES20.GL_UNSIGNED_SHORT,
            OpenGLTools.getShortBuffer(mOrders)
        )
        //直接draw
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
    }
}