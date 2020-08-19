package com.baling.camera2OpenGl

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.TextureView
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CameraCapture.CaptureListener {
    lateinit var mBinding: ActivityMainBinding
    lateinit var mSurfaceTexture: SurfaceTexture
    lateinit var mCapture: CameraCapture
    val mRenderThread = HandlerThread("render")
    lateinit var mRenderHandler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.activity_main, null, false
            )
        setContentView(mBinding.root)
        mRenderThread.start()
        mRenderHandler = Handler(mRenderThread.looper)
        mCapture = CameraCapture(this)
        mBinding.texture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return false
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                if (surface != null) {
                    mSurfaceTexture = surface
                }
                mCapture.openCamera(
                    mSurfaceTexture,
                    CameraCharacteristics.LENS_FACING_BACK,
                    mBinding.texture.width,
                    mBinding.texture.height,
                    mRenderHandler,//该参数可为空
                    this@MainActivity
                )
            }

        }
    }

    override fun onCaptureCompleted() {

    }
}