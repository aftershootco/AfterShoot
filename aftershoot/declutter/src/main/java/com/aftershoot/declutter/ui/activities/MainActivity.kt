package com.aftershoot.declutter.ui.activities

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image

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
        // TODO : move this to a local database later
        val imageList = arrayListOf<Image>()
        val blurredImageList = arrayListOf<Image>()
        val underExposeImageList = arrayListOf<Image>()
        val overExposeImageList = arrayListOf<Image>()
        val goodImageList = arrayListOf<Image>()
        val blinkImageList = arrayListOf<Image>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        shouldEnableDarkMode(DarkModeConfig.YES)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            showSliderAndLogin()
        } else {
            queryScopedStorage()
        }
    }

    private fun showSliderAndLogin() {
        val introIntent = Intent(this, IntroActivity::class.java)
        startActivityForResult(introIntent, RQ_CODE_INTRO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_CODE_INTRO && resultCode == Activity.RESULT_OK) {
            queryScopedStorage()
        } else {
            Toast.makeText(this, "Something went wrong, please restart the app!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun queryScopedStorage() {

        val scopedProjection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media._ID
        )

        val scopedSortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                scopedProjection,
                null,
                null,
                scopedSortOrder
        )

        cursor.use {
            it?.let {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getString(sizeColumn)
                    val date = it.getString(dateColumn)

                    val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                    )

                    imageList.add(Image(contentUri, null, name, size, date))

                }
            } ?: kotlin.run {
                Log.e("TAG", "Cursor is null!")
            }
        }

        val intent = Intent(this, ProgressActivity::class.java)
        startActivity(intent)
        finish()

    }

}
