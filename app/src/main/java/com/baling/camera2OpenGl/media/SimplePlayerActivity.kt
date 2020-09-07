package com.baling.camera2OpenGl.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.R
import com.baling.camera2OpenGl.camera.FileUtils
import com.baling.camera2OpenGl.databinding.ActivitySimplePlayerBinding
import com.baling.camera2OpenGl.media.media.decoder.AudioDecoder
import com.baling.camera2OpenGl.media.media.decoder.VideoDecoder
import java.util.concurrent.Executors

class SimplePlayerActivity : AppCompatActivity() {
    lateinit var mBinding: ActivitySimplePlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.activity_simple_player,
                null,
                false
            )
        val path = FileUtils.getFileDir(this)!!.path + "/video/test2.mp4"
        setContentView(mBinding.root)
        val threadPool = Executors.newFixedThreadPool(2)
        mBinding.sfv.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                val videoDecoder = VideoDecoder(path, null, holder!!.surface)
                val audioDecoder = AudioDecoder(path)
                threadPool.execute(videoDecoder)
                threadPool.execute(audioDecoder)
                videoDecoder.goOn()
                audioDecoder.goOn()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
            }

        })
    }
}