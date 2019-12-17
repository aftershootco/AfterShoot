package com.aftershoot.declutter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.R
import com.aftershoot.declutter.ui.fragments.BadImageFragment

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        supportFragmentManager.beginTransaction().add(R.id.container, BadImageFragment()).commit()
    }
}