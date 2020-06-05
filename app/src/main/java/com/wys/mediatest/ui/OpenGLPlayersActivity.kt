package com.wys.mediatest.ui

import android.os.Bundle
import android.os.Environment
import android.os.Handler
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
 * @ClassName:      OpenGLPlayersActivity
 * @Description:     OPENGL渲染多个视频
 * @Author:         wys
 * @CreateDate:     2020/6/5 17:41
 * @UpdateUser:     更新者
 * @UpdateDate:     2020/6/5 17:41
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class OpenGLPlayersActivity :AppCompatActivity(){
    val path = Environment.getExternalStorageDirectory().absolutePath + "/yinshiping/mv1.mp4"
    private val path2 = Environment.getExternalStorageDirectory().absolutePath + "/yinshiping/mv2.mp4"

    private val render = SimpleRender()

    private val threadPool = Executors.newFixedThreadPool(10)
    lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengl_player)
        initFirstVideo()
        initSecondVideo()
        initRender()    }

    private fun initFirstVideo() {
        val drawer = VideoDrawer()
        drawer.setVideoSize(288, 174)
        drawer.getSurfaceTexture {
            initPlayer(path, Surface(it), true)
        }
        render.addDrawer(drawer)
    }

    private fun initSecondVideo() {
        val drawer = VideoDrawer()
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(225, 398)
        drawer.getSurfaceTexture {
            initPlayer(path2, Surface(it), false)
        }
        render.addDrawer(drawer)
        gl_surface.addDrawer(drawer)

        Handler().postDelayed({
            drawer.scale(0.5f, 0.5f)
        }, 1000)
    }

    private fun initPlayer(path: String, sf: Surface, withSound: Boolean) {
        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)
        videoDecoder.goOn()

        if (withSound) {
            val audioDecoder = AudioDecoder(path)
            threadPool.execute(audioDecoder)
            audioDecoder.goOn()
        }
    }

    private fun initRender() {
        gl_surface.setEGLContextClientVersion(2)
        gl_surface.setRenderer(render)
    }
}