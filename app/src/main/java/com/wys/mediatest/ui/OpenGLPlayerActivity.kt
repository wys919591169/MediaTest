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
import com.wys.mediatest.opengl.drawer.VideoDrawer
import kotlinx.android.synthetic.main.activity_opengl_player.*
import java.util.concurrent.Executors

/**
 *
 * @ProjectName:    MediaTest
 * @Package:        com.wys.mediatest.ui
 * @ClassName:      OpenGLPlayerActivity
 * @Description:     java类作用描述
 * @Author:         wys
 * @CreateDate:     2020/6/4 16:59
 * @UpdateUser:     更新者
 * @UpdateDate:     2020/6/4 16:59
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class OpenGLPlayerActivity : AppCompatActivity() {
    val path = Environment.getExternalStorageDirectory().absolutePath + "/yinshiping/mvtest.mp4"
    lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengl_player)
        initRender()
    }

    private fun initRender() {
        drawer = VideoDrawer()
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