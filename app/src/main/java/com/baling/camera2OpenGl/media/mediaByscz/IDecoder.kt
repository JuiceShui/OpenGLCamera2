package com.baling.camera2OpenGl.media.mediaByscz

import android.media.MediaFormat

interface IDecoder : Runnable {
    /**
     * 暂停解码
     */
    fun pause()

    /**
     * 继续解码
     */
    fun goOn()

    /**
     * 停止解码
     */
    fun stop()

    /**
     * 是否正在解码
     */
    fun isDecoding(): Boolean

    /**
     * 是否正在拖动进度条
     */
    fun isSeeking(): Boolean

    /**
     * 是否停止解码
     */
    fun isStop(): Boolean

    /**
     * 设置状态监听器
     */
    fun setStateListener(listener: IDecoderStateListener?)

    /**
     * 获取视频宽度
     */
    fun getWidth(): Int

    /**
     * 获取视频高度
     */
    fun getHeight(): Int

    /**
     * 获取视频长度
     */
    fun getDuration(): Long

    /**
     *当前帧时间 ms
     */
    fun getCurrentTimeStamp(): Long

    /**
     * 获取视频旋转角度
     */
    fun getRotationAngle(): Int

    /**
     * 获取音视频对应格式参数
     */
    fun getMediaFormat(): MediaFormat?

    /**
     * 获取音视频对应的媒体轨道
     */
    fun getTrack(): Int

    /**
     * 获取解码的文件路径
     */
    fun getFilePath(): String

    /**
     * 拖动进度条到
     */
    fun seekTo(pos: Long): Long

    /**
     * 拖动进度条到----并且播放
     */
    fun seekAndPlay(pos: Long): Long
}