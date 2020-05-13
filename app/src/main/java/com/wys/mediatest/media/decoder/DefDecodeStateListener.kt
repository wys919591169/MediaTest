package com.wys.mediatest.media.decoder

import com.wys.mediatest.media.BaseDecoder
import com.wys.mediatest.media.Frame
import com.wys.mediatest.media.IDecoderStateListener


/**
 * 默认解码状态监听器
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @since LearningVideo
 * @version LearningVideo
 *
 */
interface DefDecodeStateListener: IDecoderStateListener {
    override fun decoderPrepare(decodeJob: BaseDecoder?) {}
    override fun decoderReady(decodeJob: BaseDecoder?) {}
    override fun decoderRunning(decodeJob: BaseDecoder?) {}
    override fun decoderPause(decodeJob: BaseDecoder?) {}
    override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame) {}
    override fun decoderFinish(decodeJob: BaseDecoder?) {}
    override fun decoderDestroy(decodeJob: BaseDecoder?) {}
    override fun decoderError(decodeJob: BaseDecoder?, msg: String) {}
}