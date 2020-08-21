package com.baling.camera2OpenGl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class GLUtils {
    //转化为floatBuffer给OpenGL使用
    fun toFloatBuffer(data: FloatArray): FloatBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(data.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()
        floatBuffer.put(data)
        floatBuffer.position(0)
        return floatBuffer
    }

    //转化为shortBuffer给OpenGL使用
    fun toShortBuffer(data: ShortArray): ShortBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(data.size * 2)
        byteBuffer.order(ByteOrder.nativeOrder())
        val shortBuffer = byteBuffer.asShortBuffer()
        shortBuffer.put(data)
        shortBuffer.position(0)
        return shortBuffer
    }
}