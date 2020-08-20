package com.baling.camera2OpenGl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Size
import android.view.Surface
import android.widget.Toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class CameraCapture(context: Context) {
    private val mCameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    var mListener: CaptureListener? = null
    private var mHandler: Handler? = null
    private lateinit var mSurface: Surface
    private var mCameraDevice: CameraDevice? = null
    var mCameraSession: CameraCaptureSession? = null
    private var mImageReader: ImageReader? = null
    private var mSensorOrientation: Int? = 0
    private val mSavePhotoExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mContext = context
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
            mCameraSession = session
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

    private val mImageReaderListener =
        ImageReader.OnImageAvailableListener {
            savePicture(it)
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
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(id)//获取相机参数
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == facing) {//获取需要的朝向相机参数
                //获取到对应方向的摄像头
                val sizes =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                        .getOutputSizes(SurfaceTexture::class.java)//获取预览surface的可用尺寸列表
                val size = getMostSuitableSize(sizes, width, height)

                val photoSizes =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                        .getOutputSizes(ImageReader::class.java)//获取拍照imageReader的可用尺寸列表
                mImageReader = getImageReader(getMostSuitableSize(photoSizes, width, height))
                preview.setDefaultBufferSize(size!!.width, size.height)//设置预览尺寸
                mSensorOrientation =
                    cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!//获取当前的手机朝向
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
        val outputs = listOf(
            mSurface,
            mImageReader!!.surface
        )//摄像头数据输出的对象可以有多个，这里传递了一个预览的surface，一个拍照用的ImageReader
        camera.createCaptureSession(outputs, mCreateSessionCallback, mHandler)
    }

    //开启预览
    fun requestPreview(session: CameraCaptureSession) {
        val builder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        builder.addTarget(mSurface)
        session.setRepeatingRequest(builder.build(), mCaptureCallback, mHandler)//开启预览
    }

    //配置imageReader
    private fun getImageReader(size: Size?): ImageReader {
        val imageReader = ImageReader.newInstance(size!!.width, size.height, ImageFormat.JPEG, 5)
        imageReader.setOnImageAvailableListener(mImageReaderListener, mHandler)
        return imageReader
    }

    //执行拍照
    fun takePicture() {
        val builder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        builder!!.addTarget(mSurface)
        builder.addTarget(mImageReader!!.surface)
        builder.set(CaptureRequest.JPEG_ORIENTATION, mSensorOrientation)
        mCameraSession!!.capture(builder.build(), mCaptureCallback, mHandler)//开始拍照
    }

    //关闭相机
    fun closeCamera() {
        mCameraDevice?.close()
        mCameraDevice = null
        mCameraSession?.close()
        mCameraSession = null
    }

    //保存图片
    private fun savePicture(reader: ImageReader) {
        val image = reader.acquireNextImage()
        val time = System.currentTimeMillis()
        val name = "Image_$time.jpg"
        mSavePhotoExecutor.execute(Runnable {
            val file = File(getWavFileDir(mContext), name)
            AndroidSchedulers.mainThread().createWorker().schedule(Runnable {
                Toast.makeText(mContext, file.path, Toast.LENGTH_SHORT).show()
            })
            val byteBuffer = image.planes[0].buffer
            val bytes = ByteArray(byteBuffer.remaining())
            byteBuffer.get(bytes)
            FileOutputStream(file).write(bytes)
            image.close()
        })

    }

    interface CaptureListener {
        fun onCaptureCompleted()
    }

    private fun getFileDir(context: Context): File? {
        var filesDir = context.getExternalFilesDir(null)
        if (filesDir == null) {
            filesDir = context.filesDir
        }
        return filesDir
    }

    private fun getWavFileDir(context: Context?): File? {
        val fileDir = getFileDir(context!!)!!
        val wavFileDir = File(fileDir, "hhh")
        if (!wavFileDir.exists()) {
            wavFileDir.mkdirs()
        }
        return wavFileDir
    }
}