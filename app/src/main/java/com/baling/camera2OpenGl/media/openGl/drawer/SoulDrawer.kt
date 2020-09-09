package com.baling.camera2OpenGl.media.openGl.drawer

import android.graphics.Path
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Size
import com.baling.camera2OpenGl.media.openGl.OpenGLTools
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class SoulDrawer : IDrawer {
    private val VERTEX = floatArrayOf(
        -1f, 1f,
        1f, 1f,
        1f, -1f,
        -1f, -1f
    )
    private val COORDINATE = floatArrayOf(
        0f, 0f,
        1f, 0f,
        1f, 1f,
        0f, 1f
    )
    private val REVERSE_VERTEX = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        1f, 1f,
        -1f, 1f
    )
    private val ORDER = shortArrayOf(
        0, 1, 2,
        2, 3, 0
    )
    private lateinit var mVideoSize: Size
    private lateinit var mScreenSize: Size
    private var mVertexHandler = -1
    private var mCoordinateHandler = -1
    private var mTextureHanlder = -1
    private var mAlphaHandler = -1
    private var mTextureId = -1
    private var mProgram = -1
    private var mMatrixHandler = -1
    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mCoordinateBuffer: FloatBuffer
    private lateinit var mOrderBuffer: ShortBuffer
    private lateinit var mMatrixBuffer: FloatBuffer
    private var mMatrix: FloatArray? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurfaceTextureCallBack: ((SurfaceTexture) -> Unit)? = null
    private var mAlpha: Float = 1f
    private var mWidthRatio = 1f
    private var mHeightRatio = 1f

    //------soul 相关变量
    //soul的FBO帧
    private var mSoulFrameBuffer: Int = -1

    //绘制soul的TextureId
    private var mSoulTextureId: Int = -1

    //soul 片元纹理句柄
    private var mSoulTextureHandler: Int = -1

    //soul缩放进度句柄
    private var mProgressHandler: Int = -1

    //是否更新FBO纹理
    private var mDrawFBO: Int = -1

    //是否更新FBO纹理的句柄
    private var mDrawFBOHandler: Int = -1

    //一帧soul的时间
    private var mModifyTime: Long = -1L

    init {
        mVertexBuffer = OpenGLTools.getFloatBuffer(VERTEX)
        mCoordinateBuffer = OpenGLTools.getFloatBuffer(COORDINATE)
        mOrderBuffer = OpenGLTools.getShortBuffer(ORDER)
    }

    override fun draw() {
        if (mTextureId != -1) {
            //initDefaultMatrix()
            initTranslateMatrix()
            createProgram()
            //FBO-------
            //更新soul纹理
            updateFBO()
            //激活soul纹理单元
            activeSoulTexture()
            //-----------
            activeTexture()
            updateTexture()
            onDraw()
        }
    }

    override fun setTextureID(id: Int) {
        mTextureId = id
        mSurfaceTexture = SurfaceTexture(mTextureId)
        mSurfaceTextureCallBack?.invoke(mSurfaceTexture!!)
    }

    override fun release() {
        GLES20.glDisableVertexAttribArray(mCoordinateHandler)
        GLES20.glDisableVertexAttribArray(mVertexHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    override fun setVideoSize(size: Size) {
        mVideoSize = size
    }

    override fun setScreenSize(size: Size) {
        mScreenSize = size
    }

    override fun setAlpha(alpha: Float) {
        mAlpha = alpha
    }

    override fun getSurfaceTexture(): SurfaceTexture? {
        return mSurfaceTexture
    }

    override fun getSurfaceTexture(cb: (surfaceTexture: SurfaceTexture) -> Unit) {
        mSurfaceTextureCallBack = cb
    }

    fun createProgram() {
        if (mProgram == -1) {
            mProgram = GLES20.glCreateProgram()
            val vertexShader =
                OpenGLTools.loadShader(GLES20.GL_VERTEX_SHADER, ShaderString.VIDEO_SHADER_VERTEX)
            val fragShader =
                OpenGLTools.loadShader(
                    GLES20.GL_FRAGMENT_SHADER,
                    ShaderString.VIDEO_SOUL_SHADER_FRAG
                )
            GLES20.glAttachShader(mProgram, vertexShader)
            GLES20.glAttachShader(mProgram, fragShader)
            GLES20.glLinkProgram(mProgram)
            mVertexHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mCoordinateHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHanlder = GLES20.glGetUniformLocation(mProgram, "uTexture")
            mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix")
            mAlphaHandler = GLES20.glGetAttribLocation(mProgram, "aAlpha")
            mSoulTextureHandler = GLES20.glGetUniformLocation(mProgram, "uSoulTexture")
            mProgressHandler = GLES20.glGetUniformLocation(mProgram, "progress")
            mDrawFBOHandler = GLES20.glGetUniformLocation(mProgram, "drawFBO")
        }
        GLES20.glUseProgram(mProgram)
    }

    fun activeTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        GLES20.glUniform1i(mTextureHanlder, 0)
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }

    fun onDraw() {
        GLES20.glEnableVertexAttribArray(mVertexHandler)
        GLES20.glEnableVertexAttribArray(mCoordinateHandler)
        GLES20.glEnableVertexAttribArray(mMatrixHandler)
        GLES20.glEnableVertexAttribArray(mAlphaHandler)
        GLES20.glUniformMatrix4fv(
            mMatrixHandler, 1,
            false, mMatrix, 0
        )
        GLES20.glVertexAttribPointer(
            mVertexHandler, 2,
            GLES20.GL_FLOAT, false,
            0, mVertexBuffer
        )
        GLES20.glVertexAttribPointer(
            mCoordinateHandler, 2,
            GLES20.GL_FLOAT, false,
            0, mCoordinateBuffer
        )
        GLES20.glVertexAttrib1f(mAlphaHandler, mAlpha)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            ORDER.size,
            GLES20.GL_UNSIGNED_SHORT,
            mOrderBuffer
        )
    }

    private fun updateTexture() {
        mSurfaceTexture?.updateTexImage()
    }

    private fun initDefaultMatrix() {
        if (mMatrix != null) {
            return
        }
        if (mScreenSize.width == -1 || mScreenSize.height == -1 ||
            mVideoSize.width == -1 || mVideoSize.height == -1
        ) {
            return
        }
        mMatrix = FloatArray(16)
        val prjMatrix = OpenGLTools.getPrjMatrix(mVideoSize, mScreenSize)
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(
            viewMatrix, 0, 0f, 0f, 5.0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        Matrix.multiplyMM(
            mMatrix, 0, prjMatrix,
            0, viewMatrix, 0
        )
    }

    fun initTranslateMatrix() {
        if (mMatrix != null) return
        if (mScreenSize.width == -1 || mScreenSize.height == -1 ||
            mVideoSize.width == -1 || mVideoSize.height == -1
        ) {
            return
        }
        mMatrix = FloatArray(16)
        val prjMatrix = FloatArray(16)
        val videoRatio = mVideoSize.width / mVideoSize.height.toFloat()
        val screenRatio = mScreenSize.width / mScreenSize.height.toFloat()
        if (mScreenSize.width > mScreenSize.height) {
            if (videoRatio > screenRatio) {
                mHeightRatio = videoRatio / screenRatio
                Matrix.orthoM(
                    prjMatrix, 0,
                    -mWidthRatio, mWidthRatio,
                    -mHeightRatio, mHeightRatio,
                    3f, 5f
                )
            } else {
                mWidthRatio = screenRatio / videoRatio
                Matrix.orthoM(
                    prjMatrix, 0,
                    -mWidthRatio, mWidthRatio,
                    -mHeightRatio, mHeightRatio,
                    3f, 5f
                )
            }
        } else {
            if (videoRatio > screenRatio) {
                mHeightRatio = videoRatio / screenRatio
                Matrix.orthoM(
                    prjMatrix, 0,
                    -mWidthRatio, mWidthRatio,
                    -mHeightRatio, mHeightRatio,
                    3f, 5f
                )
            } else {
                mWidthRatio = screenRatio / videoRatio
                Matrix.orthoM(
                    prjMatrix, 0,
                    -mWidthRatio, mWidthRatio,
                    -mHeightRatio, mHeightRatio,
                    3f, 5f
                )
            }
        }
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 5.0f,
            0f, 0f, 0f,
            0f, 1.0f, 0f
        )
        Matrix.multiplyMM(
            mMatrix, 0, prjMatrix,
            0, viewMatrix, 0
        )
    }

    fun activeSoulTexture() {

    }

    fun activeDefTexture() {

    }

    fun updateFBO() {

    }

    fun translate(dx: Float, dy: Float) {
        Matrix.translateM(
            mMatrix, 0, dx * mWidthRatio * 2,
            -dy * mHeightRatio * 2, 0f
        )
    }

    fun scale(scaleX: Float, scaleY: Float) {
        Matrix.scaleM(mMatrix, 0, scaleX, scaleY, 1f)
        mWidthRatio /= scaleX
        mHeightRatio /= scaleY
    }
}