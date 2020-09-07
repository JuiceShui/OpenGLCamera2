package com.baling.camera2OpenGl.media

import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.R
import com.baling.camera2OpenGl.camera.FileUtils
import com.baling.camera2OpenGl.databinding.ActivityOpenGlPlayerBinding
import com.baling.camera2OpenGl.media.media.decoder.AudioDecoder
import com.baling.camera2OpenGl.media.media.decoder.VideoDecoder
import com.baling.camera2OpenGl.media.openGl.SimpleRender
import com.baling.camera2OpenGl.media.openGl.drawer.IDrawer
import com.baling.camera2OpenGl.media.openGl.drawer.VideoDrawer
import java.util.concurrent.Executors

class OpenGlPlayActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityOpenGlPlayerBinding
    private lateinit var mDrawer: IDrawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.activity_open_gl_player,
            null,
            false
        )
        setContentView(mBinding.root)
        mBinding.gl.setEGLContextClientVersion(2)
        mDrawer = VideoDrawer()
        mDrawer.setVideoSize(Size(644, 352))
        mDrawer.getSurfaceTexture {
            initPlayer(Surface(it))
        }
        val render = SimpleRender()
        render.addDrawer(mDrawer)
        mBinding.gl.setRenderer(render)
    }

    private fun initPlayer(sf: Surface) {
        val path = FileUtils.getFileDir(this)!!.path + "/video/test2.mp4"
        val threadPool = Executors.newFixedThreadPool(10)
        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)
        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)
        videoDecoder.goOn()
        audioDecoder.goOn()
    }
}