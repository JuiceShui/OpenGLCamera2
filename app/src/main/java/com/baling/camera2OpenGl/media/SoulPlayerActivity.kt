package com.baling.camera2OpenGl.media

import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.R
import com.baling.camera2OpenGl.camera.FileUtils
import com.baling.camera2OpenGl.databinding.ActivitySoulPlayerBinding
import com.baling.camera2OpenGl.media.media.decoder.VideoDecoder
import com.baling.camera2OpenGl.media.openGl.SimpleRender
import com.baling.camera2OpenGl.media.openGl.drawer.IDrawer
import com.baling.camera2OpenGl.media.openGl.drawer.SoulDrawer
import java.util.concurrent.Executors

class SoulPlayerActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySoulPlayerBinding
    private lateinit var mDrawer: IDrawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this)
            , R.layout.activity_soul_player, null,
            false
        )
        setContentView(mBinding.root)
        initRender()
    }

    fun initRender() {
        mDrawer = SoulDrawer()
        mDrawer.setVideoSize(Size(644, 352))
        mDrawer.getSurfaceTexture {
            initPlayer(Surface(it))
        }
        mBinding.gl.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(mDrawer)
        mBinding.gl.setRenderer(render)
    }

    fun initPlayer(surface: Surface) {
        val path = FileUtils.getFileDir(this)!!.path + "/video/test2.mp4"
        val threadPool = Executors.newFixedThreadPool(10)

        val videoDecoder = VideoDecoder(path, null, surface)
        threadPool.execute(videoDecoder)
        videoDecoder.goOn()
    }
}