package com.baling.camera2OpenGl

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.baling.camera2OpenGl.databinding.ActivityMainBinding
import com.baling.camera2OpenGl.shader.NormalShader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class MainActivity : AppCompatActivity(), CameraCapture.CaptureListener, View.OnClickListener {
    lateinit var mBinding: ActivityMainBinding
    lateinit var mSurfaceTexture: SurfaceTexture
    var mCapture: CameraCapture? = null
    val mRenderThread = HandlerThread("render")
    lateinit var mRenderHandler: Handler
    lateinit var mSurfaceRender: SurfaceRender
    lateinit var mEGLHelper: EGLHelper
    lateinit var mCameraTexture: SurfaceTexture
    var mIsCameraOpen = false
    var mCameraFacing = CameraCharacteristics.LENS_FACING_BACK
    var mTransformMatrix = FloatArray(16)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.activity_main, null, false
            )
        setContentView(mBinding.root)
        mBinding.click = this
        mRenderThread.start()
        mRenderHandler = Handler(mRenderThread.looper)
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

            @SuppressLint("Recycle")
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                if (surface != null) {
                    mSurfaceTexture = surface
                }
                Observable.just(Surface(mSurfaceTexture))
                    .subscribeOn(AndroidSchedulers.from(mRenderHandler.looper))
                    .map {
                        mEGLHelper = EGLHelper()
                        mEGLHelper.initEGL()
                        mSurfaceRender = SurfaceRender(mEGLHelper, it, width, height)
                        mSurfaceRender.setShader(this@MainActivity, NormalShader())
                        mCameraTexture = SurfaceTexture(mEGLHelper.getTexture())
                    }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        mCapture = CameraCapture(this@MainActivity)
                        openCamera()
                    }
            }

        }
    }

    fun openCamera() {
        mIsCameraOpen = true
        mCapture!!.openCamera(
            mCameraTexture,
            mCameraFacing,
            mBinding.texture.width,
            mBinding.texture.height,
            mRenderHandler,//该参数可为空
            this@MainActivity
        )
    }

    fun closeCamera() {
        mIsCameraOpen = false;
        mCapture!!.closeCamera()
    }

    fun switchCamera() {
        mCameraFacing =
            if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK)
                CameraCharacteristics.LENS_FACING_FRONT
            else CameraCharacteristics.LENS_FACING_BACK
        closeCamera()
        openCamera()

    }

    override fun onResume() {
        super.onResume()
        makeFullscreen()
        if (!mIsCameraOpen && mCapture != null) {
            openCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    fun makeFullscreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    override fun onCaptureCompleted() {
        mCameraTexture.updateTexImage()
        mCameraTexture.getTransformMatrix(mTransformMatrix)
        mSurfaceRender.render(mTransformMatrix)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_take_photo ->
                mCapture!!.takePicture()
            R.id.tv_switch ->
                switchCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCapture!!.closeCamera()
    }
}