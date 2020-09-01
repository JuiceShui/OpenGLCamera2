package com.baling.camera2OpenGl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.view.Surface
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.microedition.khronos.opengles.GL10

class EGLHelper {
    private lateinit var mEGLDisplay: EGLDisplay
    private var mEGLConfig: EGLConfig? = null
    private var mEGLContext: EGLContext? = null
    private var mEGLSurface: EGLSurface? = null
    private lateinit var mSurfaceTexture: SurfaceTexture
    private var mTextureId: Int = -1
    fun initEGL(surfaceTexture: SurfaceTexture) {
        mSurfaceTexture = surfaceTexture
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            return
        }
        EGL14.eglInitialize(mEGLDisplay, null, 0, null, 0)
        mEGLConfig = chooseEGLConfig(mEGLDisplay)
        mEGLContext = createEGLContext(mEGLDisplay, mEGLConfig)
        mEGLSurface = createEGLSurface(Surface(mSurfaceTexture))
        makeCurrent(mEGLSurface!!)
    }

    fun initEGL() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            return
        }
        EGL14.eglInitialize(mEGLDisplay, null, 0, null, 0)
        mEGLConfig = chooseEGLConfig(mEGLDisplay)
        mEGLContext = createEGLContext(mEGLDisplay, mEGLConfig)
        //mEGLSurface = createEGLSurface(Surface(mSurfaceTexture))
        //makeCurrent(mEGLSurface!!)
    }

    //选择EGLConfig
    private fun chooseEGLConfig(eglDisplay: EGLDisplay): EGLConfig? {
        val attrList = intArrayOf(
            EGL14.EGL_BUFFER_SIZE, 32,
            EGL14.EGL_ALPHA_SIZE, 8,//指定Alpha大小，以上四项实际上指定了像素格式
            EGL14.EGL_RED_SIZE, 8, //指定RGB中的R大小（bits）
            EGL14.EGL_GREEN_SIZE, 8,//指定G大小
            EGL14.EGL_BLUE_SIZE, 8, //指定B大小
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //指定渲染api类别, 如上一小节描述，
            // 这里或者是硬编码的4(EGL14.EGL_OPENGL_ES2_BIT)
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT, //渲染类型
            EGL14.EGL_DEPTH_SIZE, 16, //指定深度缓存(Z Buffer)大小，
            EGL14.EGL_NONE //总是以EGL14.EGL_NONE结尾
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val num_configs = IntArray(1)
        val isInitConfigSuccess = EGL14.eglChooseConfig(
            eglDisplay, attrList, 0,
            configs, 0, configs.size, num_configs, 0
        )
        if (!isInitConfigSuccess) {
            return null
        }
        return configs[0]
    }

    //创建EGLContext
    private fun createEGLContext(eglDisplay: EGLDisplay, eglConfig: EGLConfig?): EGLContext {
        val attrList = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        return EGL14.eglCreateContext(
            eglDisplay,
            eglConfig, EGL14.EGL_NO_CONTEXT, attrList, 0
        )
    }

    //创建EGLSurface
    fun createEGLSurface(surface: Surface): EGLSurface {
        val attrList = intArrayOf(EGL14.EGL_NONE)
        return EGL14.eglCreateWindowSurface(
            mEGLDisplay, mEGLConfig, surface, attrList, 0
        )
    }

    fun destroyEGLSurface(surface: EGLSurface) {
        EGL14.eglDestroySurface(mEGLDisplay, surface)
    }

    //指定绘制界面
    fun makeCurrent(surface: EGLSurface) {
        EGL14.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext)
    }

    //指定绘制界面
    fun makeCurrent() {
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)
    }

    //绘制
    fun swapBuffers(eglSurface: EGLSurface?) {
        EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    fun swapBuffers() {
        EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
    }

    //完后后销毁
    fun destoryEGL() {
        EGL14.eglMakeCurrent(
            mEGLDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
        EGL14.eglTerminate(mEGLDisplay)
    }

    fun createProgram(vertexSource: InputStream, fragSource: InputStream): Int {
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, loadShader(GLES20.GL_VERTEX_SHADER, vertexSource))
        GLES20.glAttachShader(program, loadShader(GLES20.GL_FRAGMENT_SHADER, fragSource))
        GLES20.glLinkProgram(program)
        val status = intArrayOf(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            GLES20.glDeleteProgram(program)
            return -1
        }
        return program
    }

    fun deleteProgram(program: Int) {
        GLES20.glDeleteProgram(program)
    }

    fun setPresentationTime(eglSurface: EGLSurface?, nsecs: Long) {
        if (eglSurface == null) {
            return
        }
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    fun getTexture(): Int {
        if (mTextureId == -1) {
            val textures = intArrayOf(1)
            GLES20.glGenTextures(1, textures, 0)
            mTextureId = textures[0]
        }
        return mTextureId
    }

    //创建视频数据流的OES TEXTURE
    fun createTextureID(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE
        )
        return texture[0]
    }

    fun loadShader(shaderType: Int, sourceStream: InputStream): Int {
        val sourceStr = readStringFromStream(sourceStream)
        val shader = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shader, sourceStr)
        GLES20.glCompileShader(shader)
        val compiled = intArrayOf(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            //编译出错，删除
            GLES20.glDeleteShader(shader)
            return -1
        }
        return shader
    }

    //将asset文件读取为string
    fun readStringFromStream(stream: InputStream): String {
        val builder = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream))
        var line = reader.readLine()
        while (line != null) {
            builder.append(line)
                .append("\n")
            line = reader.readLine()
        }
        return builder.toString()
    }
}