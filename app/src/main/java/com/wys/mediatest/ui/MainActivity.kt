package com.wys.mediatest.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.wys.mediatest.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun clickToSound(view: View) {
        startActivity(Intent(baseContext, SoundActivity::class.java))
    }

    fun clickToSimplePlayer(view: View) {
        startActivity(Intent(baseContext, SimplePlayerActivity::class.java))

    }

    fun clickToSimpleRender(view: View) {
        val intent = Intent(this, SimpleRenderActivity::class.java)
        intent.putExtra("type", 0)
        startActivity(intent)
    }

    fun clickToSimplePic(view: View) {
        val intent = Intent(this, SimpleRenderActivity::class.java)
        intent.putExtra("type", 1)
        startActivity(intent)
    }

    fun clickToVideo(view: View) {
        startActivity(Intent(baseContext, OpenGLPlayerActivity::class.java))

    }

    fun clickToTwoVideo(view: View) {
        startActivity(Intent(baseContext, OpenGLPlayersActivity::class.java))
    }

    fun clickToEgl(view: View) {
        startActivity(Intent(baseContext, EGLPlayerActivity::class.java))

    }
}
