package com.wys.mediatest.ui

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.wys.mediatest.R
import com.wys.mediatest.media.decoder.AudioDecoder
import com.wys.mediatest.media.decoder.VideoDecoder
import com.wys.mediatest.opengl.drawer.VideoDrawer
import com.wys.mediatest.opengl.egl.CustomerGLRenderer
import kotlinx.android.synthetic.main.activity_egl_player.*
import kotlinx.android.synthetic.main.activity_opengl_player.*
import java.util.concurrent.Executors

/**
 *  author : wys
 *  date : 2020/6/7 17:42
 *  description :
 */
class EGLPlayerActivity :AppCompatActivity(){
    val path = Environment.getExternalStorageDirectory().absolutePath + "/yinshiping/mv1.mp4"
    private val path2 = Environment.getExternalStorageDirectory().absolutePath + "/yinshiping/mv2.mp4"

    private val threadPool = Executors.newFixedThreadPool(10)

    private var mRenderer = CustomerGLRenderer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_egl_player)
        initFirstVideo()
        initSecondVideo()
        setRenderSurface()
    }
    private fun initFirstVideo() {
        val drawer = VideoDrawer()
        drawer.setVideoSize(288, 174)
        drawer.getSurfaceTexture {
            initPlayer(path, Surface(it), true)
        }
        mRenderer.addDrawer(drawer)
    }

    private fun initSecondVideo() {
        val drawer = VideoDrawer()
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(225, 398)
        drawer.getSurfaceTexture {
            initPlayer(path2, Surface(it), false)
        }
        mRenderer.addDrawer(drawer)
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
    private fun setRenderSurface() {
        mRenderer.setSurface(sfv)
    }
}