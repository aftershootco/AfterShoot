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
import kotlin.math.abs


class ProgressActivity : AppCompatActivity() {

    // Our model expects a RGB image, hence the channel size is 3
    private val channelSize = 3

    // Width of the image that our model expects
    var inputImageWidth = 224
    // Height of the image that our model expects
    var inputImageHeight = 224
    // Size of the input buffer size (if your model expects a float input, multiply this with 4)
    private var modelInputSize = inputImageWidth * inputImageHeight * channelSize

    // Output you get from your model, this is essentially as we saw in netron
    val resultArray = Array(1) { ByteArray(3) }

    val interpreter by lazy {
        Interpreter(loadModelFile())
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
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

    companion object {
        // Replace the deprecated AsyncTask here R.I.P. :'(
        class LoaderTask(private val progressActivity: ProgressActivity) : AsyncTask<List<Image>, Int, Unit>() {

            override fun onPreExecute() {
                super.onPreExecute()
                progressActivity.progressBar.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg images: List<Image>) {
                val imageList = images[0]

                imageList.forEachIndexed { index, image ->
                    // Read the bitmap from a local file
                    val bitmap = BitmapFactory.decodeFile(image.file.path)
                    // Resize the bitmap so that it's 224x224
                    val resizedImage =
                            Bitmap.createScaledBitmap(bitmap, progressActivity.inputImageWidth, progressActivity.inputImageHeight, true)

                    // Convert the bitmap to a ByteBuffer
                    val modelInput = progressActivity.convertBitmapToByteBuffer(resizedImage)

                    progressActivity.interpreter.run(modelInput, progressActivity.resultArray)
                    // A number between 0-255 that tells the ratio that the images is overexposed
                    Log.d("TAG", "Overexposed : ${abs(progressActivity.resultArray[0][0].toInt())}")
                    // A number between 0-255 that tells the ratio that the images is good
                    Log.d("TAG", "Good : ${abs(progressActivity.resultArray[0][1].toInt())}")
                    // A number between 0-255 that tells the ratio that the images is underexposed
                    Log.d("TAG", "Underexposed : ${abs(progressActivity.resultArray[0][2].toInt())}")

                    publishProgress(index)
                }
            }

            override fun onProgressUpdate(vararg values: Int?) {
                super.onProgressUpdate(*values)
                progressActivity.tvStatus.text = "Processing : ${values[0]} out of ${imageList.size}"
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                progressActivity.startResultActivity()
                progressActivity.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        LoaderTask(this).execute(imageList)
    }

    private fun startResultActivity() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }
}