package com.baling.camera2OpenGl.media.mediaByscz

enum class DecodeState {
    /**
     * 开始状态
     */
    START,

    /**
     * 解码中....
     */
    DECODING,

    /**
     * 解码暂停中...
     */
    PAUSE,

    /**
     *进度条被拉取中
     */
    SEEKING,

    /**
     * 解码完成
     */
    FINISH,

    /**
     * 解码停止
     */
    STOP
}