package com.baling.camera2OpenGl.openGl.media

import android.media.MediaCodec
import android.media.MediaFormat
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer

abstract class BaseDecoder(val mFilePath: String) : IDecoder {
//--------------------------------------
// ------------状态相关------------------
// -------------------------------------
    /**
     * 解码器是否正在运行
     */
    var mIsRunning = true

    /**
     * 线程等待锁
     */
    val mLock = Object()

    /**
     * 是否可以进入解码
     */
    var mReadyForDecode = false
//------------------------------------
//--------------解码相关---------------
//------------------------------------
    /**
     * 音视频解码器
     */
    var mCodeC: MediaCodec? = null

    /**
     * 音视频数据读取器
     */
    var mExtractor: IExtractor? = null

    /**
     * 解码输入缓存区
     */
    var mInputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码输出缓存区
     */
    var mOutputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码数据信息
     */
    val mBufferInfo = MediaCodec.BufferInfo()

    /**
     *解码器当前状态
     */
    var mState = DecodeState.STOP

    /**
     * 状态监听器
     */
    var mStateListener: IDecoderStateListener? = null

    /**
     * 流数据是否结束
     */
    var mIsEOS = false

    /**
     * 视频宽度
     */
    var mVideoWidth = 0

    /**
     * 视频高度
     */
    var mVideoHeight = 0

    /**
     * 视频长度
     */
    var mDuration: Long = 0

    /**
     * 视频结束时间
     */
    var mEndPos: Long = 0

    /**
     * 开始解码时间，用于音视频同步
     */
    private var mStartTimeForSync = -1L


    override fun run() {
        mState = DecodeState.START
        mStateListener?.decoderPrepare(this)

        if (!init()) return
        try {
            while (mIsRunning) {
                if (mState != DecodeState.START &&
                    mState != DecodeState.DECODING &&
                    mState != DecodeState.SEEKING
                ) {
                    waitDecode()
                    //mStartTimeForSync = System.currentTimeMillis() - getCurrentTimeStamp()
                }
                if (!mIsRunning || mState == DecodeState.STOP) {
                    break
                }
                //如果数据没有解码完毕，将数据推入解码器解码
                if (!mIsEOS) {
                    //将数据推入解码器
                    mIsEOS = pushBufferToDecoder()
                }
                //将解码好的数据从缓冲去取出来
                val index = pullBufferFromDecoder()
                if (index > 0) {
                    render(mOutputBuffers!![index], mBufferInfo)
                    mCodeC!!.releaseOutputBuffer(index, true)
                    if (mState == DecodeState.START) {
                        mState = DecodeState.PAUSE
                    }
                }
                //判断解码是否完成
                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    mState = DecodeState.FINISH
                    mStateListener?.decoderFinish(this)
                }
            }
            doneDecode()
            release()
        } catch (e: Exception) {

        }
    }

    private fun init(): Boolean {
        //检测视频地址是否正确
        if (mFilePath.isEmpty() || !File(mFilePath).exists()) {
            mStateListener?.decoderError(this, "文件路径为空")
            return false
        }
        //调用虚函数，检测子类参数是否完整
        if (!check()) return false
        //初始化数据提取器
        mExtractor = initExtractor(mFilePath)
        if (mExtractor == null || mExtractor!!.getFormat() == null) return false
        //初始化参数
        if (!initParams()) return false

        //初始化渲染器
        if (!initRender()) return false

        //初始化解码器
        if (!initCodec()) return false

        return true

    }

    /**
     * 初始化参数
     */
    private fun initParams(): Boolean {
        try {
            val format = mExtractor!!.getFormat()!!
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
            if (mEndPos == 0L) mEndPos = mDuration
            initSpecParams(format)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 初始化编码器
     */
    private fun initCodec(): Boolean {
        try {
            val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodeC = MediaCodec.createDecoderByType(type)
            if (!configCodec(mCodeC!!, mExtractor!!.getFormat()!!)) {
                waitDecode()
            }
            mCodeC!!.start()
            mInputBuffers = mCodeC?.inputBuffers
            mOutputBuffers = mCodeC?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun waitDecode() {
        try {
            if (mState == DecodeState.PAUSE) {
                mStateListener?.decoderPause(this)
            }
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
        if (mState == DecodeState.DECODING) {
            mStateListener?.decoderRunning(this)
        }
    }

    /**
     * 从解码器获取数据
     */
    private fun pullBufferFromDecoder(): Int {
        //查询是否有解码完成的数据 index>0时，表示有数据，并且index为缓冲区索引
        val index = mCodeC!!.dequeueOutputBuffer(mBufferInfo, 1000)
        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
            }
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
            }
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                mOutputBuffers = mCodeC!!.outputBuffers
            }
            else -> {
                return index
            }
        }
        return -1
    }

    /**
     * 向解码器传递数据
     */
    private fun pushBufferToDecoder(): Boolean {
        val inputBufferIndex = mCodeC!!.dequeueInputBuffer(2000)
        var isEndOfStream = false
        if (inputBufferIndex >= 0) {
            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize = mExtractor!!.readBuffer(inputBuffer)
            if (sampleSize < 0) {
                //如果数据已经读取完毕，写入数据结束标志 BUFFER_FLAG_END_OF_STREAM
                mCodeC!!.queueInputBuffer(
                    inputBufferIndex,
                    0,
                    0,
                    0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            } else {
                mCodeC!!.queueInputBuffer(
                    inputBufferIndex,
                    0,
                    sampleSize,
                    mExtractor!!.getCurrentTimestamp(),
                    0
                )
            }
        }
        return isEndOfStream
    }

    private fun release() {
        try {
            mState = DecodeState.STOP
            mIsEOS = false
            mExtractor?.stop()
            mCodeC?.stop()
            mCodeC?.release()
            mStateListener?.decoderDestroy(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 检测子类参数
     */
    abstract fun check(): Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(path: String): IExtractor

    /**
     * 初始化子类独有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    /**
     * 渲染
     */
    abstract fun render(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo)

    /**
     * 结束解码
     */
    abstract fun doneDecode()

    override fun getCurrentTimeStamp(): Long {
        return mBufferInfo.presentationTimeUs / 1000
    }
}