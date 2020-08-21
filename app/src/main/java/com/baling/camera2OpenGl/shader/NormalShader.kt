package com.baling.camera2OpenGl.shader

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.baling.camera2OpenGl.EGLHelper
import com.baling.camera2OpenGl.GLUtils
import java.nio.FloatBuffer
import java.nio.ShortBuffer

abstract class NormalShader : IShader {
    val VERTIC_SHADER = "shader.vert.glsl"
    val FRAGMENT_SHADER = "shader.frag.glsl"
    val VERTICES = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f, 1.0f,
        -1.0f, 1.0f
    )

    val TEXTURE_COORD = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    val ORDERS = shortArrayOf(
        0, 1, 2,
        2, 3, 0
    )
    lateinit var mHelper: EGLHelper
    lateinit var mVertices: FloatBuffer
    lateinit var mCoords: FloatBuffer
    lateinit var mOrders: ShortBuffer
    lateinit var mContext: Context
    var mProgram: Int = 0
    var mPositionHandle = 0
    var mTexPreviewHandle = 0
    var mCoordsHandle = 0
    var mTransformMatrixHandle = 0
    var mColorMatrixHandle = 0

    override fun onAttach(ctx: Context, helper: EGLHelper) {
        mContext = ctx
        mHelper = helper
        val assetManager = ctx.assets
        mProgram = mHelper.createProgram(
            assetManager.open(VERTIC_SHADER),
            assetManager.open(FRAGMENT_SHADER)
        )
        GLES20.glUseProgram(mProgram)
        mVertices = GLUtils().toFloatBuffer(VERTICES)
        mCoords = GLUtils().toFloatBuffer(TEXTURE_COORD)
        mOrders = GLUtils().toShortBuffer(ORDERS)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mCoordsHandle = GLES20.glGetAttribLocation(mProgram, "vCoord")
        mTexPreviewHandle = GLES20.glGetUniformLocation(mProgram, "texPreview")
        mColorMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uColorMatrix")
        mTransformMatrixHandle = GLES20.glGetUniformLocation(mProgram, "matTransform")
    }

    override fun onDetach() {
        mHelper.deleteProgram(mProgram)
        mProgram = -1
    }

    override fun draw(helper: EGLHelper, transformMatrix: FloatArray, oesTexture: Int) {
        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexture)
        GLES20.glUniform1i(mTexPreviewHandle, 0)

        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertices)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        GLES20.glVertexAttribPointer(mCoordsHandle, 2, GLES20.GL_FLOAT, false, 0, mCoords)
        GLES20.glEnableVertexAttribArray(mCoordsHandle)

        GLES20.glUniformMatrix4fv(mColorMatrixHandle, 1, true, getEffectMatrix(), 0)
        GLES20.glUniformMatrix4fv(mTransformMatrixHandle, 1, false, transformMatrix, 0)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, ORDERS.size, GLES20.GL_UNSIGNED_SHORT, mOrders)
    }

    abstract fun getEffectMatrix(): FloatArray
}