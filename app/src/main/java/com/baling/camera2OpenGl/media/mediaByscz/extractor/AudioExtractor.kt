package com.baling.camera2OpenGl.media.mediaByscz.extractor

import android.media.MediaFormat
import com.baling.camera2OpenGl.media.mediaByscz.IExtractor
import java.nio.ByteBuffer

class AudioExtractor(path: String) : IExtractor {
    private val mMediaExtractor = MMExtractor(path)
    override fun getFormat(): MediaFormat? {
        return mMediaExtractor.getAudioFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mMediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimestamp(): Long {
        return mMediaExtractor.getCurrentTimestamp()
    }

    override fun seek(pos: Long): Long {
        return mMediaExtractor.getCurrentTimestamp()
    }

    override fun setStartPos(pos: Long) {
        mMediaExtractor.setStartPos(pos)
    }

    override fun stop() {
        mMediaExtractor.stop()
    }
}