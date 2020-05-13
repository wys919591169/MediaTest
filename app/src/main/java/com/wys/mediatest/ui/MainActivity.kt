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
}
