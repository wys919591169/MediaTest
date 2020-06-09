package com.wys.mediatest.ui

import android.os.Bundle
import android.os.Environment
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.wys.mediatest.R
import com.wys.mediatest.media.decoder.AudioDecoder
import com.wys.mediatest.media.decoder.VideoDecoder
import com.wys.mediatest.opengl.SimpleRender
import com.wys.mediatest.opengl.drawer.IDrawer
import com.wys.mediatest.opengl.drawer.SoulVideoDrawer
import kotlinx.android.synthetic.main.activity_opengl_player.*
import java.util.concurrent.Executors

/**
 *  author : wys
 *  date : 2020/6/9 23:19
 *  description :fbo
 */
class FBOPlayerActivity :AppCompatActivity(){
    private val path = Environment.getExternalStorageDirectory().absolutePath + "/yinshiping/mv2.mp4"
    lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengl_player)
        initRender()
    }

    private fun initRender() {
        // 使用“灵魂出窍”渲染器
        drawer = SoulVideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(Surface(it))
        }
        gl_surface.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer)
        gl_surface.setRenderer(render)
    }

    private fun initPlayer(sf: Surface) {
        val threadPool = Executors.newFixedThreadPool(10)

        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)

        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.goOn()
        audioDecoder.goOn()
    }
}