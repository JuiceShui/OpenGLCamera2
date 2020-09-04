package com.baling.camera2OpenGl.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.MediaRecorder
import android.opengl.EGLSurface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.*
import android.view.View.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baling.camera2OpenGl.BuildConfig
import com.baling.camera2OpenGl.R
import com.baling.camera2OpenGl.databinding.ActivityMainBinding
import com.baling.camera2OpenGl.camera.encoder.VideoEncoder
import com.baling.camera2OpenGl.camera.shader.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), CameraCapture.CaptureListener, OnClickListener,
    ShaderAdapter.OnSelectShaderListener {

    private val VIDEO_BIT_RATE = 1024 * 1024 * 1024
    private val VIDEO_FRAME_RATE = 30
    private val AUDIO_BIT_RATE = 44800

    lateinit var mBinding: ActivityMainBinding
    lateinit var mSurfaceTexture: SurfaceTexture
    lateinit var mRenderHandler: Handler
    lateinit var mSurfaceRender: SurfaceRender
    lateinit var mEGLHelper: EGLHelper
    lateinit var mCameraTexture: SurfaceTexture

    var mCapture: CameraCapture? = null
    val mRenderThread = HandlerThread("render")
    var mIsCameraOpen = false
    var mCameraFacing = CameraCharacteristics.LENS_FACING_BACK
    var mTransformMatrix = FloatArray(16)
    var mMediaRecorder: MediaRecorder? = null
    var mRecordSurface: EGLSurface? = null
    var mLastVideo: File? = null
    var mEncoder: VideoEncoder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.activity_main, null, false
            )
        setContentView(mBinding.root)
        val shaders =
            object : ArrayList<ShaderAdapter.ShaderInfo?>() {
                init {
                    add(
                        ShaderAdapter.ShaderInfo(
                            R.string.shader_normal,
                            NormalShader::class.java
                        )
                    )
                    add(
                        ShaderAdapter.ShaderInfo(
                            R.string.shader_decolor,
                            DecolorShader::class.java
                        )
                    )
                    add(
                        ShaderAdapter.ShaderInfo(
                            R.string.shader_reversal,
                            ReverseShader::class.java
                        )
                    )
                    add(
                        ShaderAdapter.ShaderInfo(
                            R.string.shader_nostalgia,
                            NostalgiaShader::class.java
                        )
                    )
                }
            }
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        mBinding.shaderSelector.layoutManager = layoutManager
        val adaptor = ShaderAdapter(
            mBinding.shaderSelector,
            shaders
        )
        adaptor.setOnSelectShaderListener(this)
        mBinding.shaderSelector.adapter = adaptor
        mBinding.click = this
        mRenderThread.start()
        mRenderHandler = Handler(mRenderThread.looper)
        mBinding.record.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    //mediaRecorder录制
                    // mRecordSurface = startRecord(genFileName())
                    //mediaCodeC 编码
                    mRecordSurface = startRecord()
                } else {
                    if (mMediaRecorder != null) {
                        stopRecord()
                    }
                    if (mEncoder != null) {
                        stopEncode()
                    }
                }
            }
        })
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
                        mSurfaceRender =
                            SurfaceRender(
                                mEGLHelper,
                                it,
                                width,
                                height
                            )
                        mSurfaceRender.setShader(this@MainActivity, NormalShader())
                        mCameraTexture = SurfaceTexture(mEGLHelper.getTexture())
                    }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        mCapture =
                            CameraCapture(this@MainActivity)
                        openCamera()
                    }
            }

        }
    }

    override fun onSelectShader(shader: IShader?) {
        mRenderHandler.post { mSurfaceRender.setShader(this@MainActivity, shader!!) }
    }

    override fun onCaptureCompleted() {
        mCameraTexture.updateTexImage()
        mCameraTexture.getTransformMatrix(mTransformMatrix)
        mSurfaceRender.render(mTransformMatrix)
        if (mRecordSurface != null) {
            mSurfaceRender.render(mTransformMatrix, mRecordSurface!!)
            mEGLHelper.setPresentationTime(mRecordSurface, mSurfaceTexture.timestamp)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.take_photo ->
                mCapture!!.takePicture()
            R.id.switch_camera ->
                switchCamera()
            R.id.play_video -> {
                val uri = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".fileprovider",
                    mLastVideo!!
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(uri, "video/*")
                startActivity(intent)
            }
            R.id.select_shader -> {
                mBinding.shaderSelector.visibility = VISIBLE
                mBinding.selectShader.visibility = GONE
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mBinding.shaderSelector.visibility == VISIBLE
            && event!!.action == MotionEvent.ACTION_UP
        ) {
            mBinding.selectShader.visibility = VISIBLE
            mBinding.shaderSelector.visibility = GONE
        }
        return super.onTouchEvent(event)
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

    override fun onDestroy() {
        super.onDestroy()
        mCapture!!.closeCamera()
    }

    private fun openCamera() {
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

    private fun closeCamera() {
        mIsCameraOpen = false;
        mCapture!!.closeCamera()
    }

    private fun switchCamera() {
        mCameraFacing =
            if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK)
                CameraCharacteristics.LENS_FACING_FRONT
            else CameraCharacteristics.LENS_FACING_BACK
        closeCamera()
        openCamera()

    }

    /**
     * 全屏显示
     */
    private fun makeFullscreen() {
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    fun genFileName(): String {
        return "video_" + System.currentTimeMillis() + ".mp4"

    }

    /**
     * mediaRecorder 录制
     */
    fun startRecord(fileName: String): EGLSurface {
        mBinding.playVideo.visibility = GONE
        mLastVideo = File(FileUtils.getMediaFileDir(this), fileName)
        mMediaRecorder = MediaRecorder()
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder!!.setOutputFile(mLastVideo!!.path)
        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mMediaRecorder!!.setVideoEncodingBitRate(VIDEO_BIT_RATE)
        mMediaRecorder!!.setVideoSize(mBinding.texture.width, mBinding.texture.height)
        mMediaRecorder!!.setVideoFrameRate(VIDEO_FRAME_RATE)
        mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mMediaRecorder!!.setAudioEncodingBitRate(AUDIO_BIT_RATE)
        mMediaRecorder!!.setOrientationHint(0)
        try {
            mMediaRecorder!!.prepare()
        } catch (e: Exception) {
            Toast.makeText(this, "MediaRecorder failed on prepare()", Toast.LENGTH_LONG).show()
        }
        mMediaRecorder!!.start()
        return mEGLHelper.createEGLSurface(mMediaRecorder!!.surface)
    }

    /**
     * mediaCodeC 编码
     */
    fun startRecord(): EGLSurface {
        mEncoder = VideoEncoder()
        mEncoder!!.initConfig(mBinding.texture.width, mBinding.texture.height)
        mEncoder!!.start()
        return mEGLHelper.createEGLSurface(mEncoder!!.getInputSurface())
    }

    fun stopRecord() {
        mBinding.playVideo.visibility = VISIBLE
        mMediaRecorder!!.stop()
        mMediaRecorder!!.release()
        mMediaRecorder = null
        mEGLHelper.destroyEGLSurface(mRecordSurface!!)
        mRecordSurface = null
    }

    fun stopEncode() {
        mEncoder!!.stop()
    }
}