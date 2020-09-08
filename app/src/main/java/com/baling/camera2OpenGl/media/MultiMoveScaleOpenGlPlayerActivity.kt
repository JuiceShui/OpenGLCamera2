package com.baling.camera2OpenGl.media

import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.R
import com.baling.camera2OpenGl.camera.FileUtils
import com.baling.camera2OpenGl.databinding.ActivityMultiMoveScaleOpenGlPlayerBinding
import com.baling.camera2OpenGl.databinding.ActivityMultiOpenGlPlayerBinding
import com.baling.camera2OpenGl.media.media.decoder.VideoDecoder
import com.baling.camera2OpenGl.media.openGl.SimpleRender
import com.baling.camera2OpenGl.media.openGl.drawer.VideoDrawer
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class MultiMoveScaleOpenGlPlayerActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMultiMoveScaleOpenGlPlayerBinding
    private val render = SimpleRender()
    private val threadPool = Executors.newFixedThreadPool(10)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.activity_multi_move_scale_open_gl_player,
            null,
            false
        )
        setContentView(mBinding.root)
        initFirstDrawer()
        initSecondDrawer()
        initRender()
    }

    fun initFirstDrawer() {
        val path = FileUtils.getFileDir(this)!!.path + "/video/test.mp4"
        val drawer = VideoDrawer()
        drawer.setVideoSize(Size(1280, 720))
        drawer.getSurfaceTexture {
            initPlayer(path, Surface(it))
        }
        render.addDrawer(drawer)
    }

    fun initSecondDrawer() {
        val path = FileUtils.getFileDir(this)!!.path + "/video/test2.mp4"
        val drawer = VideoDrawer()
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(Size(644, 352))
        drawer.getSurfaceTexture {
            initPlayer(path, Surface(it))
        }
        render.addDrawer(drawer)
        mBinding.gl.addDrawer(drawer)
        Handler().postDelayed({
            drawer.scale(0.5f,0.5f)
        },1000)
    }

    fun initPlayer(path: String, surface: Surface) {
        val player = VideoDecoder(path, null, surface)
        threadPool.execute(player)
        player.goOn()
    }

    fun initRender() {
        mBinding.gl.setEGLContextClientVersion(2)
        mBinding.gl.setRenderer(render)
    }
}