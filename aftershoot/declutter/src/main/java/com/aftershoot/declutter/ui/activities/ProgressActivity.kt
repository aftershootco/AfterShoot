package com.aftershoot.declutter.ui.activities

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.imageList
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.activity_progress.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class ProgressActivity : AppCompatActivity() {

    val interpreter by lazy {
        Interpreter(loadModelFile())
    }

    val CHANNEL_SIZE = 3

    lateinit var inputShape: IntArray

    var inputImageWidth = 0
    var inputImageHeight = 0
    var modelInputSize = 0

    val resultArray = Array(1) { ByteArray(3) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = inputImageWidth * inputImageHeight * CHANNEL_SIZE

        LoaderTask().execute(imageList)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun setupAnimation() {
        val animation = findViewById<LottieAnimationView>(R.id.progressBar)
        animation.speed = 2.0F // How fast does the animation play
        animation.progress = 50F // Starts the animation from 50% of the beginning
        animation.addAnimatorUpdateListener {
            // Called everytime the frame of the animation changes
        }
        animation.repeatMode = LottieDrawable.RESTART // Restarts the animation (you can choose to reverse it as well)
        animation.cancelAnimation() // Cancels the animation
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputImageWidth) {
            for (j in 0 until inputImageHeight) {
                val pixelVal = pixels[pixel++]
                byteBuffer.put((pixelVal shr 16 and 0xFF).toByte())
                byteBuffer.put((pixelVal shr 8 and 0xFF).toByte())
                byteBuffer.put((pixelVal and 0xFF).toByte())
            }
        }
        bitmap.recycle()

        return byteBuffer
    }

    inner class LoaderTask : AsyncTask<List<Image>, Int, Unit>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg images: List<Image>) {

            images[0].forEachIndexed { index, image ->

                // Need a better way to reuse and recycle bitmaps here
                val bitmap = BitmapFactory.decodeFile(image.file.path)
                val resizedImage =
                        Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)

                val input = convertBitmapToByteBuffer(resizedImage)

                interpreter.run(input, resultArray)

                Log.e("TAG", "Probabilities are ${resultArray[0][0]}, ${resultArray[0][1]} and ${resultArray[0][2]}")
                publishProgress(index)
            }

        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            tvStatus.text = "Processing : ${values[0]} out of ${imageList.size}"
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            startResultActivity()
            finish()
        }
    }

    private fun startResultActivity() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }

}