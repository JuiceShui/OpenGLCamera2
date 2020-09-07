package com.baling.camera2OpenGl.media.openGl.drawer

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Size
import com.baling.camera2OpenGl.media.openGl.OpenGLTools
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL

class BitmapDrawer(bitmap: Bitmap) : IDrawer {

    private val VERTEX = floatArrayOf(
        -1f, 1f,
        1f, 1f,
        1f, -1f,
        -1f, -1f
    )
    private val COORDS = floatArrayOf(
        0f, 0f,
        1f, 0f,
        1f, 1f,
        0f, 1f
    )
    private val mOrder = shortArrayOf(
        0, 1, 2,
        2, 3, 0
    )
    private val mBitmap = bitmap
    private var mTextureId = -1

    //顶点坐标
    private var mVertexHandler = -1

    //纹理坐标
    private var mCoordsHandler = -1

    //纹理
    private var mTextureHanlder = -1

    private var mProgram = -1
    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mCoordsBuffer: FloatBuffer

    init {
        mVertexBuffer = OpenGLTools.getFloatBuffer(VERTEX)
        mCoordsBuffer = OpenGLTools.getFloatBuffer(COORDS)
    }

    override fun draw() {
        createProgram()
        activeTexture()
        bindBitmapToTexture()
        onDraw()
    }

    override fun setTextureID(id: Int) {
        mTextureId = id
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

    fun createProgram() {
        if (mProgram == -1) {
            mProgram = GLES20.glCreateProgram()
            val shaderVertex =
                OpenGLTools.loadShader(GLES20.GL_VERTEX_SHADER, ShaderString.BITMAP_SHADER_VERTEX)
            val shaderFrag =
                OpenGLTools.loadShader(GLES20.GL_FRAGMENT_SHADER, ShaderString.BITMAP_SHADER_FRAG)
            GLES20.glAttachShader(mProgram, shaderFrag)
            GLES20.glAttachShader(mProgram, shaderVertex)
            GLES20.glLinkProgram(mProgram)
            mVertexHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mCoordsHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHanlder = GLES20.glGetUniformLocation(mProgram, "uTexture")
        }
        GLES20.glUseProgram(mProgram)
    }

    fun activeTexture() {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理id到指定纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, mTextureId)
        //将激活的纹理单元传递到着色器
        GLES20.glUniform1i(mTextureHanlder, 0)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE
        )
    }

    /**
     * 绑定bitmap到纹理
     */
    fun bindBitmapToTexture() {
        if (!mBitmap.isRecycled) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
        }
    }

    fun onDraw() {
        GLES20.glEnableVertexAttribArray(mVertexHandler)
        GLES20.glVertexAttribPointer(
            mTextureHanlder, 2,
            GLES20.GL_FLOAT, false, 0, mVertexBuffer
        )
        GLES20.glEnableVertexAttribArray(mCoordsHandler)
        GLES20.glVertexAttribPointer(
            mCoordsHandler, 2,
            GLES20.GL_FLOAT, false, 0, mCoordsBuffer
        )
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            OpenGLTools.getShortBuffer(mOrder)
        )
    }
}