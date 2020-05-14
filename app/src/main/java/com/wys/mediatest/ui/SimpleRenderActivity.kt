package com.wys.mediatest.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wys.mediatest.R
import com.wys.mediatest.opengl.SimpleRender
import com.wys.mediatest.opengl.drawer.BitmapDrawer
import com.wys.mediatest.opengl.drawer.IDrawer
import com.wys.mediatest.opengl.drawer.TriangleDrawer
import kotlinx.android.synthetic.main.activity_simpler_render.*

/**
 *
 * @ProjectName:    MediaTest
 * @Package:        com.wys.mediatest.ui
 * @ClassName:      SimpleRenderActivity
 * @Description:     简单渲染页面
 * @Author:         wys
 * @CreateDate:     2020/5/13 16:45
 */
class SimpleRenderActivity : AppCompatActivity() {
    private lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simpler_render)

        drawer = if (intent.getIntExtra("type", 0) == 0) {
            TriangleDrawer()
        } else {
            BitmapDrawer(BitmapFactory.decodeResource(baseContext!!.resources, R.drawable.cover))
        }
        initRender(drawer)
    }

    private fun initRender(drawer: IDrawer) {
        gl_surface.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer)
        gl_surface.setRenderer(render)
    }

    override fun onDestroy() {
        drawer.release()
        super.onDestroy()
    }
}