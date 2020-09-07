package com.baling.camera2OpenGl.media

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.R
import com.baling.camera2OpenGl.databinding.ActivitySimpleRenderBinding
import com.baling.camera2OpenGl.media.openGl.SimpleRender
import com.baling.camera2OpenGl.media.openGl.drawer.BitmapDrawer
import com.baling.camera2OpenGl.media.openGl.drawer.TriangleDrawer

class SimpleRenderActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySimpleRenderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*mBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.activity_simple_render,
                null,
                false
            )*/
        setContentView(R.layout.activity_simple_render)
        val gl = findViewById<GLSurfaceView>(R.id.gl)
        gl.setEGLContextClientVersion(2)
        val render = SimpleRender()
        // render.addDrawer(TriangleDrawer())
        render.addDrawer(
            BitmapDrawer(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.test
                )
            )
        )
        gl.setRenderer(render)
    }
}