package com.wys.mediatest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.media.AudioRecord

import android.media.AudioFormat


class MainActivity : AppCompatActivity() {
    /**
     * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
     */
    val SAMPLE_RATE_INHZ = 44100

    /**
     * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
     */
    val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    /**
     * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
     */
    val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val audioRecord: AudioRecord? = null  // 声明 AudioRecord 对象
    private var recordBufSize = 0 // 声明recoordBufffer的大小字段
    fun createrAudio(){
        recordBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ,CHANNEL_CONFIG,AUDIO_FORMAT)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
    }
}
