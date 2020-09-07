package com.baling.camera2OpenGl.media.mediaByscz

import android.media.MediaFormat
import java.nio.ByteBuffer

interface IExtractor {
    /**
     * 获取音视频格式参数
     */
    fun getFormat(): MediaFormat?

    /**
     * 获取音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimestamp(): Long

    /**
     * seek到指定位置，并返回当前实际帧的时间戳
     */
    fun seek(pos: Long): Long

    /**
     * 设置开始位置的时间戳
     */
    fun setStartPos(pos: Long)

    /**
     * 停止读取数据
     */
    fun stop()
}