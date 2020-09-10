package com.baling.camera2OpenGl.media.openGl

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Size
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL

object OpenGLTools {

    fun getFloatBuffer(array: FloatArray): FloatBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(array.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()
        floatBuffer.put(array)
        floatBuffer.position(0)
        return floatBuffer
    }

    fun getShortBuffer(array: ShortArray): ShortBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(array.size * 2)
        byteBuffer.order(ByteOrder.nativeOrder())
        val shortBuffer = byteBuffer.asShortBuffer()
        shortBuffer.put(array)
        shortBuffer.position(0)
        return shortBuffer
    }

    fun loadShader(type: Int, shaderSource: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderSource)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun genTexture(count: Int): IntArray {
        val texture = IntArray(count)
        GLES20.glGenTextures(count, texture, 0)
        return texture
    }

    fun getPrjMatrix(videoSize: Size, screenSize: Size): FloatArray {
        val videoRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
        val screenRatio = screenSize.width.toFloat() / screenSize.height.toFloat()
        val prjMatrix = FloatArray(16)
        if (screenSize.width > screenSize.height) {
            val actualRatio = screenRatio * videoRatio
            if (videoRatio > screenRatio) {
                Matrix.orthoM(
                    prjMatrix, 0,
                    -actualRatio, actualRatio,
                    -1f, 1f, -1f, 6f
                )
            } else {
                Matrix.orthoM(
                    prjMatrix, 0,
                    -1f, 1f,
                    -actualRatio, actualRatio,
                    -1f, 6f
                )
            }
        } else {
            val actualRatio = videoRatio / screenRatio
            if (videoRatio > screenRatio) {
                Matrix.orthoM(
                    prjMatrix, 0,
                    -1f, 1f,
                    -actualRatio, actualRatio,
                    -1f, 6f
                )
            } else {
                Matrix.orthoM(
                    prjMatrix, 0,
                    -actualRatio, actualRatio,
                    -1f, 1f,
                    -1f, 6f
                )
            }
        }
        return prjMatrix
    }

    fun getWidthHeightRatio(videoSize: Size, screenSize: Size): FloatArray {
        val videoRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
        val screenRatio = screenSize.width.toFloat() / screenSize.height.toFloat()
        val widthHeightArray = floatArrayOf(1f, 1f)
        if (screenSize.width > screenSize.height) {
            if (videoRatio > screenRatio) {
                widthHeightArray[1] = videoRatio / screenRatio
            } else {
                widthHeightArray[0] = screenRatio / videoRatio
            }
        } else {
            if (videoRatio > screenRatio) {
                widthHeightArray[1] = videoRatio / screenRatio
            } else {
                widthHeightArray[0] = screenRatio / videoRatio
            }
        }
        return widthHeightArray
    }

    //创建FBO纹理
    fun createFBOTexture(width: Int, height: Int): Int {
        val texture = IntArray(1)
        //新建纹理
        GLES20.glGenTextures(1, texture, 0)
        //绑定纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
        //根据颜色参数，宽高等信息，为上面的纹理id，生成一个2d纹理
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return texture[0]
    }

    //新建frameBuffer 返回frameBuffer的索引
    fun createFrameBuffer(): Int {
        val fbs = IntArray(1)
        GLES20.glGenFramebuffers(1, fbs, 0)
        return fbs[0]
    }

    //绑定FBO
    //绑定创建好的framebuffer索引，
    //然后将framebuffer的TextureID绑定在GL_COLOR_ATTACHMENT0上
    fun bindFBO(fb: Int, textureId: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, textureId, 0
        )
    }

    //解绑FBO
    fun unbindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
    }

    //删除fbo
    fun deleteFBO(frame: IntArray, texture: IntArray) {
        //删除frameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glDeleteFramebuffers(1, frame, 0)
        //删除纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glDeleteTextures(1, texture, 0)
    }
}

