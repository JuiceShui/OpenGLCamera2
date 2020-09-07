package com.baling.camera2OpenGl.media.openGl.drawer

import android.graphics.SurfaceTexture
import android.util.Size
import android.view.SurfaceHolder

interface IDrawer {
    fun draw()
    fun setTextureID(id: Int)
    fun release()
    fun setVideoSize(size: Size)
    fun setScreenSize(size: Size)
    fun setAlpha(alpha: Float)
    fun getSurfaceTexture(): SurfaceTexture?
    fun getSurfaceTexture(cb: (surfaceTexture: SurfaceTexture) -> Unit) {}
}