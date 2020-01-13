package com.aftershoot.declutter.ui.activities

import android.net.Uri
import android.os.Bundle
import android.transition.Explode
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.aftershoot.declutter.R
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BigImageViewer.initialize(GlideImageLoader.with(applicationContext))
        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            // set an exit transition
            exitTransition = Explode()
            enterTransition = Explode()

            // shows the transition for 2 seconds
            exitTransition.duration = 2000

        }
        setContentView(R.layout.activity_image)
        val uri = intent.getStringExtra("URI")

        // uri of the image
        val inputStream = contentResolver.openInputStream(Uri.parse(uri))
        val exifInterface = ExifInterface(requireNotNull(inputStream))

        var rotation = 0

        when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
        }

        imageView.showImage(Uri.parse(uri))
        imageView.rotation = rotation.toFloat()
    }

}