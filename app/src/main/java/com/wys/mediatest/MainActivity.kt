package com.wys.mediatest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_to_sound.setOnClickListener {
            val intent = Intent(baseContext, SoundActivity::class.java)
            startActivity(intent)
        }
    }
}
