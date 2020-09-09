package com.baling.camera2OpenGl.media.openGl.egl

enum class RenderState {
    NO_SURFACE,//没有有效的surface
    FRESH_SURFACE,//持有一个未初始化的surface
    SURFACE_CHANGE,//surface 尺寸变化
    RENDERING,//初始化完毕，开始渲染
    SURFACE_DESTROY,//surface被销毁
    STOP//停止渲染
}