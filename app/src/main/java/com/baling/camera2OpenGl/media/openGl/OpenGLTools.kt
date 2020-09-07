package com.baling.camera2OpenGl.media.openGl

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Size
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class OpenGLTools {

    companion object {
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
    }
}