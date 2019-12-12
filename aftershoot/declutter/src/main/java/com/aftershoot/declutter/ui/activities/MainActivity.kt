package com.aftershoot.declutter.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    enum class DarkModeConfig {
        YES,
        NO,
        FOLLOW_SYSTEM
    }

    private fun shouldEnableDarkMode(darkModeConfig: DarkModeConfig) {
        when (darkModeConfig) {
            DarkModeConfig.YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            DarkModeConfig.NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DarkModeConfig.FOLLOW_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    //Execute order 66
    val RQ_CODE_INTRO = 66

    companion object {
        //for now, move this to a local database later :)
        val imageList = arrayListOf<Image>()
    }

    var uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // Add scoped storage compatibility
    var projection = arrayOf(MediaStore.MediaColumns.DATA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle the dark theme toggle based on user action
//        shouldEnableDarkMode(DarkModeConfig.YES)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            showSliderAndLogin()
        } else {
            queryStorage()
        }
    }

    private fun showSliderAndLogin() {
        val introIntent = Intent(this, IntroActivity::class.java)
        startActivityForResult(introIntent, RQ_CODE_INTRO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_CODE_INTRO && resultCode == Activity.RESULT_OK) {
            queryStorage()
            btnDone.setOnClickListener {
                val intent = Intent(this, ProgressActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Something went wrong, please restart the app!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun queryStorage() {
        val query = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
        )
        tvProgress.text = getString(R.string.loading_local_images)
        query.use { cursor ->

            cursor?.let {

                while (cursor.moveToNext()) {
                    val absolutePathOfImage = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    imageList.add(Image(File(absolutePathOfImage)))
                }
            }
        }

        val intent = Intent(this, ProgressActivity::class.java)
        startActivity(intent)
        finish()
    }

}
