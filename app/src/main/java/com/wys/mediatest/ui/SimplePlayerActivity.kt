package com.wys.mediatest.ui

import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.wys.mediatest.R
import com.wys.mediatest.media.decoder.AudioDecoder
import com.wys.mediatest.media.decoder.VideoDecoder
import com.wys.mediatest.media.muxer.MP4Repack
import kotlinx.android.synthetic.main.activity_simple_player.*
import java.util.concurrent.Executors

/**
 *
 * @ProjectName:    MediaDemo
 * @Package:        com.wys.app
 * @ClassName:      SimplePlayerActivty
 * @Description:     简单播放器页面
 * @Author:         wys
 * @CreateDate:     2020/5/11 16:09
 */
class SimplePlayerActivity:AppCompatActivity(){
    val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest_2.mp4"
    private lateinit var videoDecoder: VideoDecoder
    private lateinit var audioDecoder: AudioDecoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_player)
        initPlayer()
    }

    private fun initPlayer() {
        val threadPool = Executors.newFixedThreadPool(10)

        videoDecoder = VideoDecoder(path, sfv, null)
        threadPool.execute(videoDecoder)

        audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.goOn()
        audioDecoder.goOn()
    }

    fun clickRepack(view: View) {
        repack()
    }

    private fun repack() {
        val repack = MP4Repack(path)
        repack.start()
    }

    override fun onDestroy() {
        videoDecoder.stop()
        audioDecoder.stop()
        super.onDestroy()
    }
}