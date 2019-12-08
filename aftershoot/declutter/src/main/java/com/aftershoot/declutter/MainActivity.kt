package com.aftershoot.declutter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aftershoot.declutter.model.Image
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            showSliderAndLogin()
        } else {
            queryStorage()
            btnDone.setOnClickListener {
                val intent = Intent(this, ProgressActivity::class.java)
                startActivity(intent)
                finish()
            }
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
//        progressBar.visibility = View.VISIBLE
        tvProgress.text = getString(R.string.loading_local_images)
        query.use { cursor ->

            cursor?.let {

                while (cursor.moveToNext()) {
                    val absolutePathOfImage = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    imageList.add(Image(File(absolutePathOfImage)))
                }
            }
        }

//        progressBar.visibility = View.INVISIBLE
        tvProgress.text = "${imageList.size} images found"
        btnDone.visibility = View.VISIBLE
    }

}
