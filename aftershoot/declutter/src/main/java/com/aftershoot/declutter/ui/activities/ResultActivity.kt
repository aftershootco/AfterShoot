package com.aftershoot.declutter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.R
import com.aftershoot.declutter.ui.fragments.BadImageFragment

class ResultActivity : AppCompatActivity() {

    val badImageFragment by lazy {
        BadImageFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        supportFragmentManager.beginTransaction().add(R.id.container, badImageFragment).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        badImageFragment.notifyBackPressed()
    }

}