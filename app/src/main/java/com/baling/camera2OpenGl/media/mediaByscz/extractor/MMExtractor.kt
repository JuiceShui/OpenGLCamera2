package com.baling.camera2OpenGl.media.mediaByscz.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class MMExtractor(path: String) {
    /**
     * 音视频数据分离器
     */
    private var mExtractor: MediaExtractor? = null

    /**
     * 视频索引轨道
     */
    private var mVideoTrack: Int = -1

    /**
     * 音频索引轨道
     */
    private var mAudioTrack: Int = -1

    /**
     * 当前帧时间戳
     */
    private var mCurSampleTime: Long = 0

    /**
     * 开始解码时间点
     */
    private var mStartPos: Long = 0

    init {
        //初始化
        mExtractor = MediaExtractor()
        mExtractor?.setDataSource(path)
    }

    /**
     * 获取视频格式参数
     */
    fun getVideoFormat(): MediaFormat? {
        for (i in 0 until mExtractor!!.trackCount) {
            //获取对应i的format
            val format = mExtractor!!.getTrackFormat(i)
            //获取当前位置的编码格式
            val mime = format.getString(MediaFormat.KEY_MIME)
            //如果是video，则赋值通道索引
            if (mime!!.startsWith("video/")) {
                mVideoTrack = i
                break
            }
        }
        //返回视频格式信息
        return if (mVideoTrack > 0)
            mExtractor!!.getTrackFormat(mVideoTrack)
        else
            null
    }

    /**
     * 获取音频格式参数
     */
    fun getAudioFormat(): MediaFormat? {
        for (i in 0 until mExtractor!!.trackCount) {
            val format = mExtractor!!.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                mAudioTrack = i
                break
            }
        }
        return if (mAudioTrack > 0)
            mExtractor!!.getTrackFormat(mAudioTrack)
        else
            null
    }

    /**
     * 选择通道
     */
    private fun selectSourceTrack() {
        if (mVideoTrack > 0) {
            mExtractor!!.selectTrack(mVideoTrack)
        } else if (mAudioTrack > 0) {
            mExtractor!!.selectTrack(mAudioTrack)
        }
    }

    /**
     * 读取音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int {
        byteBuffer.clear()
        //选择音视频通道
        selectSourceTrack()
        //读取对应数据
        val readSampleCount = mExtractor!!.readSampleData(byteBuffer, 0)
        //读取到的数据量<0,表明读取结束
        if (readSampleCount < 0) {
            return -1
        }
        //赋值当前帧解码时间戳
        mCurSampleTime = mExtractor!!.sampleTime
        //进入下一帧
        mExtractor!!.advance()
        //返回读取到的数据量
        return readSampleCount
    }

    /**
     * seek到指定位置，并返回实际帧的时间戳
     */
    fun seekTo(pos: Long): Long {
        //SEEK_TO_PREVIOUS_SYNC 跳播位置的上一个关键帧
        //SEEK_TO_CLOSEST_SYNC 跳播位置最近的一个关键帧
        //SEEK_TO_NEXT_SYNC 跳播位置的下一个关键帧
        mExtractor!!.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return mExtractor!!.sampleTime
    }

    /**
     * 停止读取数据
     */
    fun stop() {
        //释放解码器
        mExtractor?.release()
        mExtractor = null
    }

    fun getVideoTrack(): Int {
        return mVideoTrack
    }

    fun getAudioTrack(): Int {
        return mAudioTrack
    }

    fun getCurrentTimestamp(): Long {
        return mCurSampleTime
    }

    fun setStartPos(pos: Long) {
        mStartPos = pos
    }
}