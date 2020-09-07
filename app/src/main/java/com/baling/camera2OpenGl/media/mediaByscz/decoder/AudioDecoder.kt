package com.baling.camera2OpenGl.media.mediaByscz.decoder

import android.media.*
import com.baling.camera2OpenGl.media.mediaByscz.BaseDecoder
import com.baling.camera2OpenGl.media.mediaByscz.IExtractor
import com.baling.camera2OpenGl.media.mediaByscz.extractor.AudioExtractor
import java.lang.Exception
import java.nio.ByteBuffer

class AudioDecoder(path: String) : BaseDecoder(path) {
    /**
     * 采样率
     */
    private var mSampleRate = -1

    /**
     * 声音通道数量
     */
    private var mChannels = 1

    /**
     * PCM采样位数
     */
    private var mPCMMEncodeBit = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 音频播发器
     */
    private var mAudioTrack: AudioTrack? = null

    /**
     * 音频数据缓存
     */
    private var mAudioOutTemBuf: ShortArray? = null

    override fun check(): Boolean {
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return AudioExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
        //获取音频的独有参数
        try {
            mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            mPCMMEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                format.getInteger(MediaFormat.KEY_PCM_ENCODING)
            } else {
                //如果没有这个参数，则设置成默认的16位采样
                AudioFormat.ENCODING_PCM_16BIT
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initRender(): Boolean {
        val channel = if (mChannels == 1) {
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            AudioFormat.CHANNEL_OUT_STEREO
        }
        val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMMEncodeBit)
        mAudioOutTemBuf = ShortArray(minBufferSize / 2)
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,//播放类型，音乐
            mSampleRate,//采样率
            channel,//通道
            mPCMMEncodeBit,//采样位数
            minBufferSize,//缓冲区大小
            AudioTrack.MODE_STREAM//播放模式：数据动态写入//另一种是一次性写入
        )
        return true
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, null, null, 0)
        return true
    }

    override fun render(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (mAudioOutTemBuf!!.size < bufferInfo.size / 2) {
            mAudioOutTemBuf = ShortArray(bufferInfo.size / 2)
        }
        byteBuffer.position(0)
        byteBuffer.asShortBuffer().get(mAudioOutTemBuf, 0, mAudioOutTemBuf!!.size)
        mAudioTrack?.write(mAudioOutTemBuf!!, 0, mAudioOutTemBuf!!.size)
    }

    override fun doneDecode() {
        mAudioTrack?.stop()
        mAudioTrack?.release()
    }
}