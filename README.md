# MediaTest
# 音视频学习之初级入门篇
## 一、AudioRecord API详解
AudioRecord是Android系统提供的用于实现录音的功能类。

要想了解这个类的具体的说明和用法，我们可以去看一下官方的文档：

　　AndioRecord类的主要功能是让各种JAVA应用能够管理音频资源，以便它们通过此类能够录制声音相关的硬件所收集的声音。此功能的实现就是通过”pulling”（读取）AudioRecord对象的声音数据来完成的。在录音过程中，应用所需要做的就是通过后面三个类方法中的一个去及时地获取AudioRecord对象的录音数据. AudioRecord类提供的三个获取声音数据的方法分别是read(byte[], int, int), read(short[], int, int), read(ByteBuffer, int). 无论选择使用那一个方法都必须事先设定方便用户的声音数据的存储格式。

　　开始录音的时候，AudioRecord需要初始化一个相关联的声音buffer, 这个buffer主要是用来保存新的声音数据。这个buffer的大小，我们可以在对象构造期间去指定。它表明一个AudioRecord对象还没有被读取（同步）声音数据前能录多长的音(即一次可以录制的声音容量)。声音数据从音频硬件中被读出，数据大小不超过整个录音数据的大小（可以分多次读出），即每次读取初始化buffer容量的数据。

实现Android录音的流程为：

1.构造一个AudioRecord对象，其中需要的最小录音缓存buffer大小可以通过getMinBufferSize方法得到。如果buffer容量过小，将导致对象构造的失败。  
2.初始化一个buffer，该buffer大于等于AudioRecord对象用于写声音数据的buffer大小。  
3.开始录音  
4.创建一个数据流，一边从AudioRecord中读取声音数据到初始化的buffer，一边将buffer中数据导入数据流。  
5.关闭数据流  
6.停止录音  
## 二、使用 AudioRecord 实现录音，并生成wav  
```
        //创建AudioRecord对象
        recordBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
            CHANNEL_CONFIG, AUDIO_FORMAT, recordBufSize
        )
        //初始化buffer
        val data = ByteArray(recordBufSize)
        val file = File(getExternalFilesDir(DIRECTORY_MUSIC), "test.pcm")
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        if (file.exists()) {
            file.delete()
        }
        //开始录音
        audioRecord!!.startRecording()
        isRecording = true

        //写入文件
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
        
        //修改录音标志位
        isRecording = false
        // 释放资源
        if (null != audioRecord) {
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
            //recordingThread = null;
        }  
```
## 三、简单处理一下录音的初始文件  
现在的文件里面的内容仅仅是最原始的音频数据，术语称为raw（中文解释是“原材料”或“未经处理的东西”），这时候，你让播放器去打开，它既不知道保存的格式是什么，又不知道如何进行解码操作。所以会播放不了。
我们简单的处理一下：在文件的数据开头加入WAVE HEAD数据，也就是文件头。只有加上文件头部的数据，播放器才能正确的知道里面的内容到底是什么，进而能够正常的解析并播放里面的内容。具体的头文件的描述，在Play a WAV file on an AudioTrack里面可以进行了解。
下面的工具类就是简单处理文件的
```
class PcmToWavUtil
/**
 * @param sampleRate sample rate、采样率
 * @param channel channel、声道
 * @param encoding Audio data format、音频格式
 */
internal constructor(
    /**
     * 采样率
     */
    private val mSampleRate: Int,
    /**
     * 声道数
     */
    private val mChannel: Int, encoding: Int
) {

    /**
     * 缓存的音频大小
     */
    private val mBufferSize: Int


    init {
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encoding)
    }


    /**
     * pcm文件转wav文件
     *
     * @param inFilename 源文件路径
     * @param outFilename 目标文件路径
     */
    fun pcmToWav(inFilename: String, outFilename: String) {
        val `in`: FileInputStream
        val out: FileOutputStream
        val totalAudioLen: Long
        val totalDataLen: Long
        val longSampleRate = mSampleRate.toLong()
        val channels = if (mChannel == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val byteRate = (16 * mSampleRate * channels / 8).toLong()
        val data = ByteArray(mBufferSize)
        try {
            `in` = FileInputStream(inFilename)
            out = FileOutputStream(outFilename)
            totalAudioLen = `in`.channel.size()
            totalDataLen = totalAudioLen + 36

            writeWaveFileHeader(
                out, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate
            )
            while (`in`.read(data) != -1) {
                out.write(data)
            }
            `in`.close()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    /**
     * 加入wav文件头
     */
    @Throws(IOException::class)
    private fun writeWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long
    ) {
        val header = ByteArray(44)
        // RIFF/WAVE header
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        //WAVE
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        // 'fmt ' chunk
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // format = 1
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        // block align
        header[32] = (2 * 16 / 8).toByte()
        header[33] = 0
        // bits per sample
        header[34] = 16
        header[35] = 0
        //data
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }
}
```
## 四、AudioTrack 基本使用
AudioTrack 类可以完成Android平台上音频数据的输出任务。AudioTrack有两种数据加载模式（MODE_STREAM和MODE_STATIC），对应的是数据加载模式和音频流类型， 对应着两种完全不同的使用场景。

MODE_STREAM：在这种模式下，通过write一次次把音频数据写到AudioTrack中。这和平时通过write系统调用往文件中写数据类似，但这种工作方式每次都需要把数据从用户提供的Buffer中拷贝到AudioTrack内部的Buffer中，这在一定程度上会使引入延时。为解决这一问题，AudioTrack就引入了第二种模式。
MODE_STATIC：这种模式下，在play之前只需要把所有数据通过一次write调用传递到AudioTrack中的内部缓冲区，后续就不必再传递数据了。这种模式适用于像铃声这种内存占用量较小，延时要求较高的文件。但它也有一个缺点，就是一次write的数据不能太多，否则系统无法分配足够的内存来存储全部数据。
### 4.1 MODE_STATIC
MODE_STATIC模式输出音频的方式如下（注意：如果采用STATIC模式，须先调用write写数据，然后再调用play。）：
```
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
```
### 4.2MODE_STREAM模式  
MODE_STREAM 模式输出音频的方式如下：
```
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
```
### 4.3 AudioTrack构造过程
每一个音频流对应着一个AudioTrack类的一个实例，每个AudioTrack会在创建时注册到 AudioFlinger中，由AudioFlinger把所有的AudioTrack进行混合（Mixer），然后输送到AudioHardware中进行播放，目前Android同时最多可以创建32个音频流，也就是说，Mixer最多会同时处理32个AudioTrack的数据流。 
![image](https://github.com/wys919591169/MediaTest/blob/master/image/audio_track.png.png)  

### 4.4 AudioTrack 与 MediaPlayer 的对比
#### 4.4.1 区别
其中最大的区别是MediaPlayer可以播放多种格式的声音文件，例如MP3，AAC，WAV，OGG，MIDI等。MediaPlayer会在framework层创建对应的音频解码器。而AudioTrack只能播放已经解码的PCM流，如果对比支持的文件格式的话则是AudioTrack只支持wav格式的音频文件，因为wav格式的音频文件大部分都是PCM流。AudioTrack不创建解码器，所以只能播放不需要解码的wav文件。

#### 4.4.2 联系
MediaPlayer在framework层还是会创建AudioTrack，把解码后的PCM数流传递给AudioTrack，AudioTrack再传递给AudioFlinger进行混音，然后才传递给硬件播放,所以是MediaPlayer包含了AudioTrack。

#### 4.4.3 SoundPool
在接触Android音频播放API的时候，发现SoundPool也可以用于播放音频。下面是三者的使用场景：MediaPlayer 更加适合在后台长时间播放本地音乐文件或者在线的流式资源; SoundPool 则适合播放比较短的音频片段，比如游戏声音、按键声、铃声片段等等，它可以同时播放多个音频; 而 AudioTrack 则更接近底层，提供了非常强大的控制能力，支持低延迟播放，适合流媒体和VoIP语音电话等场景。


参考https://www.cnblogs.com/renhui/p/7452572.html
