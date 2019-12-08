package com.aftershoot.declutter

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aftershoot.declutter.model.Image
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //Execute order 66
    val RQ_CODE_INTRO = 66

    val imageList = mutableListOf<Image>()
    val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.DISPLAY_NAME
    )
    val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        } else {
            Toast.makeText(this, "Something went wrong, please restart the app!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun queryStorage() {
        val query = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )
//        progressBar.visibility = View.VISIBLE
        tvProgress.text = getString(R.string.loading_local_images)
        query.use { cursor ->

            cursor?.let {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getString(dateAddedColumn)
                    val size = cursor.getInt(sizeColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                    )

                    imageList.add(Image(contentUri, name, size, dateAdded))
                }
            }
        }

//        progressBar.visibility = View.INVISIBLE
        tvProgress.text = "${imageList.size} images found"
        btnDone.visibility = View.VISIBLE
    }

}
