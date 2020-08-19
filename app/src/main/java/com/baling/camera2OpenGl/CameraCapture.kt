package com.baling.camera2OpenGl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.StateCallback
import android.os.Handler
import android.util.Size
import android.view.Surface

class CameraCapture(mContext: Context) {
    private val mCameraManager: CameraManager =
        mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    var mListener: CaptureListener? = null
    var mHandler: Handler?=null
    lateinit var mSurface: Surface
    lateinit var mCameraDevice: CameraDevice
    private val mOpenCameraCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            openCameraSession(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
        }

        override fun onError(camera: CameraDevice, error: Int) {
        }

    }

    private val mCreateSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
        }

        override fun onConfigured(session: CameraCaptureSession) {
            requestPreview(session)
        }

    }


    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            mListener!!.onCaptureCompleted()
        }
    }


    @SuppressLint("Recycle", "MissingPermission")
    fun openCamera(
        preview: SurfaceTexture,
        facing: Int,
        width: Int,
        height: Int,
        handler: Handler?,
        listener: CaptureListener
    ) {
        mHandler = handler
        mSurface = Surface(preview)
        mListener = listener

        for (id in mCameraManager.cameraIdList) {
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(id)
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                //获取到对应方向的摄像头
                val sizes =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                        .getOutputSizes(SurfaceTexture::class.java)
                val size = getMostSuitableSize(sizes, width, height)
                preview.setDefaultBufferSize(size!!.width, size.height)
                mCameraManager.openCamera(
                    id, mOpenCameraCallback, handler
                )
                break
            }
        }
    }

    //获取最适合的大小
    private fun getMostSuitableSize(sizes: Array<Size>, width: Int, height: Int): Size? {
        val targetRatio = height.toFloat() / width.toFloat()
        var result: Size? = null
        for (size in sizes) {
            if (result == null || isMoreSuitable(result, size, targetRatio)) {
                result = size
            }
        }
        return result
    }

    //是否是更适合的大小
    private fun isMoreSuitable(current: Size?, target: Size, targetRatio: Float): Boolean {
        if (current == null) {//当前没有最适合的尺寸，那当前就是最适合的
            return true
        }
        val dCurrentRatio = Math.abs(getRatio(current) - targetRatio)//获取当前的差值
        val dTargetRatio = Math.abs(getRatio(target) - targetRatio)//获取新目标的差值
        return dTargetRatio < dCurrentRatio || (dCurrentRatio == dTargetRatio
                && (getArea(target) > getArea(current)))//如果新目标更接近，或者新目标与当前相等，则取面积大的
    }

    //获取当前size的面积
    private fun getArea(size: Size): Int {
        return size.width * size.height
    }

    //返回当前size的比列
    private fun getRatio(size: Size): Float {
        return (size.width.toFloat() / size.height.toFloat())
    }

    fun openCameraSession(camera: CameraDevice) {
        mCameraDevice = camera
        val outputs = listOf(mSurface)
        camera.createCaptureSession(outputs, mCreateSessionCallback, mHandler)

    }

    fun requestPreview(session: CameraCaptureSession) {
        val builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        builder.addTarget(mSurface)
        session.setRepeatingRequest(builder.build(), mCaptureCallback, mHandler)
    }

    interface CaptureListener {
        fun onCaptureCompleted()
    }
}