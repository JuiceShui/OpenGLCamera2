package com.baling.camera2OpenGl.openGl.media

interface IDecoderStateListener {
    /**
     * 准备解码器
     */
    fun decoderPrepare(decoder: BaseDecoder?)

    /**
     * 解码器准备完成
     */
    fun decoderReady(decoder: BaseDecoder?)

    /**
     * 解码器运行中
     */
    fun decoderRunning(decoder: BaseDecoder?)

    /**
     * 解码器暂停中
     */
    fun decoderPause(decoder: BaseDecoder?)

    /**
     *正在解码一帧数据
     */
    fun decodeOneFrame(decoder: BaseDecoder?, frame: Frame)

    /**
     * 解码完成
     */
    fun decoderFinish(decoder: BaseDecoder?)

    /**
     * 解码器释放
     */
    fun decoderDestroy(decoder: BaseDecoder?)

    /**
     * 解码出错
     */
    fun decoderError(decoder: BaseDecoder?, msg: String)
}