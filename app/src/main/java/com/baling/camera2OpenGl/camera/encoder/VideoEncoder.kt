package com.baling.camera2OpenGl.camera.encoder

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.EGLSurface
import android.view.Surface
import com.baling.camera2OpenGl.camera.App
import com.baling.camera2OpenGl.camera.EGLHelper
import com.baling.camera2OpenGl.camera.FileUtils
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class VideoEncoder {
    var mCodeC: MediaCodec? = null
    lateinit var mVideoFormat: MediaFormat
    var mWidth = 0
    var mHeight = 0
    lateinit var mInputSurface: Surface
    lateinit var mEglSurface: EGLSurface
    var configByte: ByteArray? = null
    lateinit var mVideoSps: ByteArray
    lateinit var mVideoPps: ByteArray
    private var outputStream: BufferedOutputStream? = null
    var mInputTextureId = 0
    var mInputSurfaceTexture: SurfaceTexture? = null
    val helper: EGLHelper = EGLHelper()
    var mEncodeDisposable: Disposable? = null
    var mIsEncoding = false

    init {
        createFile()
        helper.initEGL()
    }

    /**
     * 初始化配置参数
     */
    fun initConfig(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        mVideoFormat = MediaFormat.createVideoFormat("video/avc", width, height)
        mVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
        mVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        mVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 3)
        mVideoFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        mCodeC = MediaCodec.createEncoderByType(mVideoFormat.getString(MediaFormat.KEY_MIME)!!)
        //此处不能用mOutputSurface，会configure失败
        mCodeC!!.configure(mVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mInputSurface = mCodeC!!.createInputSurface()//获取到编码器提供的渲染surface
        mCodeC!!.start()
    }

    fun getInputSurface(): Surface {
        return mInputSurface
    }

    fun getEglSurface(): EGLSurface {
        mEglSurface = helper.createEGLSurface(mInputSurface)
        return mEglSurface
    }

    fun getTextureId(): Int {
        return mInputTextureId
    }

    fun getSurfaceTexture(): SurfaceTexture? {
        mInputTextureId = helper.getTexture()
        mInputSurfaceTexture = SurfaceTexture(mInputTextureId)
        return mInputSurfaceTexture
    }

    /**
     * 开始编码
     */
    fun start() {
        mIsEncoding = true
        mEncodeDisposable = Schedulers.newThread().createWorker().schedule {
            val bufferInfo = MediaCodec.BufferInfo()
            while (mIsEncoding) {//当处于正在编码时进行循环读取数据
                var outputIndex = mCodeC!!.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {//当编码器开始返回数据开始
                    //获取数据的配置头信息
                    var byteBuffer = mCodeC!!.outputFormat.getByteBuffer("csd-0")
                    mVideoSps = ByteArray(byteBuffer!!.remaining())
                    byteBuffer.get(mVideoSps, 0, mVideoSps.size)

                    byteBuffer = mCodeC!!.outputFormat.getByteBuffer("csd-1")
                    mVideoPps = ByteArray(byteBuffer!!.remaining())
                    byteBuffer.get(mVideoPps, 0, mVideoPps.size)
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                } else if (outputIndex > 0) {//图像数据流到达
                    var singleData = byteArrayOf()
                    while (outputIndex >= 0) {//循环读取
                        val outputBuffer = mCodeC!!.getOutputBuffer(outputIndex)
                        val outData = ByteArray(bufferInfo.size)
                        outputBuffer!!.get(outData)//读取数据到buffer
                        if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                            configByte = ByteArray(bufferInfo.size)
                            configByte = outData
                        } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                            if (configByte == null || configByte!!.isEmpty()) {
                                configByte = outData
                            }
                            val head = byteMerger(mVideoSps, mVideoPps)
                            var keyframe = ByteArray(bufferInfo.size + configByte!!.size)
                            configByte = byteMerger(head, configByte!!)
                            val out = byteMerger(configByte!!, outData)
                            keyframe = byteMerger(out, keyframe)
                            if (singleData.isEmpty()) {
                                singleData = keyframe
                            } else {
                                singleData = byteMerger(singleData, keyframe)
                            }
                        } else {
                            if (singleData.isEmpty()) {
                                singleData = outData
                            } else {
                                singleData = byteMerger(singleData, outData)
                            }
                        }
                        mCodeC!!.releaseOutputBuffer(outputIndex, false)
                        outputIndex = mCodeC!!.dequeueOutputBuffer(bufferInfo, 1_000)
                        try {
                            outputStream!!.write(singleData, 0, singleData.size)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                }
            }
            //结束，释放资源
            mCodeC!!.signalEndOfInputStream()
            mEncodeDisposable!!.dispose()
            mEncodeDisposable = null
            mCodeC!!.stop()
            mCodeC!!.release()
            mCodeC = null
            outputStream?.flush()
            outputStream?.close()
        }
    }

    /**
     * 结束编码
     */
    fun stop() {
        mIsEncoding = false
    }

    /**
     * 合并两个数据
     *
     * @param bt1 数据1
     * @param bt2 数据2
     * @return 合并后的数据
     */
    private fun byteMerger(bt1: ByteArray, bt2: ByteArray): ByteArray {
        val bt3 = ByteArray(bt1.size + bt2.size)
        System.arraycopy(bt1, 0, bt3, 0, bt1.size)
        System.arraycopy(bt2, 0, bt3, bt1.size, bt2.size)
        return bt3
    }

    /**
     * 创建本地文件
     */
    private fun createFile() {
        val file = File(
            FileUtils.getFileDir(App.getInstance()),
            "codec_${System.currentTimeMillis()}.mp4"
        )
        if (file.exists()) {
            file.delete()
        }
        try {
            outputStream = BufferedOutputStream(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}