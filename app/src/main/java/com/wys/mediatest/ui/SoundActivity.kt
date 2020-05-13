package com.wys.mediatest.ui

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.*
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wys.mediatest.R
import com.wys.mediatest.media.PcmToWavUtil
import kotlinx.android.synthetic.main.activity_sound_recording.*
import java.io.*


/**
 *  author : wys
 *  date : 2020/4/24 15:50
 *  description :
 */
class SoundActivity : AppCompatActivity() {
    val TAG = "sound_record"
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

    private val MY_PERMISSIONS_REQUEST = 1001

    private var audioRecord: AudioRecord? = null  // 声明 AudioRecord 对象
    private var recordBufSize = 0 // 声明recoordBufffer的大小字段
    private var isRecording: Boolean = false

    /**
     * 需要申请的运行时权限
     */
    private val permissions = arrayOf(
        RECORD_AUDIO,
        WRITE_EXTERNAL_STORAGE
    )
    /**
     * 被用户拒绝的权限列表
     */
    private val mPermissionList: MutableList<String> = mutableListOf()
    private var audioTrack: AudioTrack? = null
    private var audioData: ByteArray? = null
    private var fileInputStream: FileInputStream? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_recording)
        checkPermissions()

        btn_sound.setOnClickListener {
            if (btn_sound.text == "开始录音") {
                btn_sound.text = "停止录音"
                startRecord()
            } else {
                btn_sound.text = "开始录音"
                stopRecord()
            }
        }
        btn_convert.setOnClickListener {
            val pcmToWavUtil = PcmToWavUtil(
                SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            val pcmFile = File(getExternalFilesDir(DIRECTORY_MUSIC), "test.pcm")
            val wavFile = File(getExternalFilesDir(DIRECTORY_MUSIC), "test.wav")
            if (!wavFile.mkdirs()) {
                Log.e(TAG, "wavFile Directory not created")
            }
            if (wavFile.exists()) {
                wavFile.delete()
            }
            pcmToWavUtil.pcmToWav(pcmFile.absolutePath, wavFile.absolutePath)
        }
        btn_play.setOnClickListener {

            if (btn_play.text == "播放") {
            btn_play.text = "停止播放"
                playInModeStream()
                //playInModeStatic();
            } else {
                btn_play.text = "播放"
                stopPlay()
            }
        }
    }

    fun startRecord() {
        recordBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
            CHANNEL_CONFIG, AUDIO_FORMAT, recordBufSize
        )
        val data = ByteArray(recordBufSize)
        val file = File(getExternalFilesDir(DIRECTORY_MUSIC), "test.pcm")
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        if (file.exists()) {
            file.delete()
        }
        audioRecord!!.startRecording()
        isRecording = true

        // TODO: 2018/3/10 pcm数据无法直接播放，保存为WAV格式。

        Thread(Runnable {
            var os: FileOutputStream? = null
            try {
                os = FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (null != os) {
                while (isRecording) {
                    val read = audioRecord!!.read(data, 0, recordBufSize)
                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os!!.write(data)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }
                try {
                    Log.i(TAG, "run: close file output stream !")
                    os!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }).start()
    }

    fun stopRecord() {
        isRecording = false
        // 释放资源
        if (null != audioRecord) {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
            //recordingThread = null;
        }
    }

    private fun checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permissions[i]
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    mPermissionList.add(permissions[i])
                }
            }
            if (mPermissionList.isNotEmpty()) {

                ActivityCompat.requestPermissions(this,
                    mPermissionList.toTypedArray(), MY_PERMISSIONS_REQUEST)
            }
        }
    }
    /**
     * 播放，使用stream模式
     */
    private fun playInModeStream() {
        /*
        * SAMPLE_RATE_INHZ 对应pcm音频的采样率
        * channelConfig 对应pcm音频的声道
        * AUDIO_FORMAT 对应pcm音频的格式
        * */
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val minBufferSize =
            AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, channelConfig, AUDIO_FORMAT)
        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                .setEncoding(AUDIO_FORMAT)
                .setChannelMask(channelConfig)
                .build(),
            minBufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack!!.play()

        val file = File(getExternalFilesDir(DIRECTORY_MUSIC), "test.pcm")
        try {
            fileInputStream = FileInputStream(file)
            Thread(Runnable {
                try {
                    val tempBuffer = ByteArray(minBufferSize)
                    while (fileInputStream!!.available() > 0) {
                        val readCount = fileInputStream!!.read(tempBuffer)
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                            continue
                        }
                        if (readCount != 0 && readCount != -1) {
                            when (audioTrack!!.playState) {
                                AudioTrack.PLAYSTATE_PLAYING -> {
                                    audioTrack!!.write(tempBuffer, 0, readCount)
                                }
                            }

                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }).start()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    /**
     * 播放，使用static模式
     */
    private fun playInModeStatic() {
        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区

        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    val `in` = resources.openRawResource(R.raw.ding)
                    `in`.use { `in` ->
                        val out = ByteArrayOutputStream()
                        var b: Int?
                        do {
                            b = `in`.read()
                            if (b==-1){
                                break
                            }
                            out.write(b)
                        }while (true)
                        Log.d(TAG, "Got the data")
                        audioData = out.toByteArray()
                    }
                } catch (e: IOException) {
                    Log.wtf(TAG, "Failed to read", e)
                }
                return null
            }
            override fun onPostExecute(v: Void?) {
                Log.i(TAG, "Creating track...audioData.length = " + audioData!!.size)

                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
                audioTrack = AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                    AudioFormat.Builder().setSampleRate(22050)
                        .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    audioData!!.size,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                Log.d(TAG, "Writing audio data...")
                audioTrack!!.write(audioData!!, 0, audioData!!.size)
                Log.d(TAG, "Starting playback")
                audioTrack!!.play()
                Log.d(TAG, "Playing")
            }

        }.execute()

    }


    /**
     * 停止播放
     */
    private fun stopPlay() {
        if (audioTrack != null) {
            Log.d(TAG, "Stopping")
            audioTrack!!.stop()
            Log.d(TAG, "Releasing")
            audioTrack!!.release()
            Log.d(TAG, "Nulling")
        }
    }
}