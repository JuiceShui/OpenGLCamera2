package com.baling.camera2OpenGl.media.mediaByscz.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.baling.camera2OpenGl.media.mediaByscz.BaseDecoder
import com.baling.camera2OpenGl.media.mediaByscz.IExtractor
import com.baling.camera2OpenGl.media.mediaByscz.extractor.VideoExtractor
import java.nio.ByteBuffer

class VideoDecoder(mFilePath: String, surface: Surface?) : BaseDecoder(mFilePath) {

    private val mSurface = surface

    override fun check(): Boolean {
        return mSurface != null
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0)
            notifyDecode()
        }
        return true
    }

    override fun render(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
    }

    override fun doneDecode() {
    }

    override fun isDecoding(): Boolean {
        return false
    }

    override fun isStop(): Boolean {
        return false
    }

}