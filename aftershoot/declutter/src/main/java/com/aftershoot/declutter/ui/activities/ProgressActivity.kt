package com.aftershoot.declutter.ui.activities

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.blurredImageList
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.goodImageList
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.imageList
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.overExposeImageList
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.underExposeImageList
import kotlinx.android.synthetic.main.activity_progress.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var inputImageWidth = 224
    // Height of the image that our model expects
    private var inputImageHeight = 224
    // Size of the input buffer size (if your model expects a float input, multiply this with 4)
    private var modelInputSize = inputImageWidth * inputImageHeight * channelSize

    private val exposureInterpreter by lazy {
        Interpreter(loadModelFile("exposure.tflite"))
    }

    private val blurInterpreter by lazy {
        Interpreter(loadModelFile("blur.tflite"))
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(modelName)
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

    private suspend fun processImage(image: Image, index: Int) {
        withContext(Dispatchers.Default) {

            val bitmap = BitmapFactory.decodeFile(image.file.path)
            // Resize the bitmap so that it's 224x224
            val resizedImage =
                    Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)

            // Convert the bitmap to a ByteBuffer
            val modelInput = convertBitmapToByteBuffer(resizedImage)

            exposureInference(modelInput, image)
            blurInference(modelInput, image)

            withContext(Dispatchers.Main) {
                tvStatus.text = "Processing : ${index} out of ${imageList.size}"
            }
        }
    }

    private fun blurInference(buffer: ByteBuffer, image: Image) {
        // Output you get from your model, this is essentially as we saw in netron
        val resultArray = Array(1) { ByteArray(2) }

        blurInterpreter.run(buffer, resultArray)
        // A number between 0-255 that tells the ratio that the images is blurred
        Log.d("TAG", "Blurred : ${abs(resultArray[0][0].toInt())}")
        // A number between 0-255 that tells the ratio that the images is unblurred
        Log.d("TAG", "Not Blurred : ${abs(resultArray[0][1].toInt())}")

        val blurredPercentage = resultArray[0][0] / 255

        if (blurredPercentage > 0.6f) {
            // the image is blurred
            blurredImageList.add(image)
        } else
            goodImageList.add(image)
    }

    private fun blinkInference(image: Image) {
        // TODO: add Firebase MLKit API calls here
    }

    private fun exposureInference(buffer: ByteBuffer, image: Image) {

        // Output you get from your model, this is essentially as we saw in netron
        val resultArray = Array(1) { ByteArray(3) }

        exposureInterpreter.run(buffer, resultArray)
        // A number between 0-255 that tells the ratio that the images is overexposed
        Log.d("TAG", "Overexposed : ${abs(resultArray[0][0].toInt())}")
        // A number between 0-255 that tells the ratio that the images is good
        Log.d("TAG", "Good : ${abs(resultArray[0][1].toInt())}")
        // A number between 0-255 that tells the ratio that the images is underexposed
        Log.d("TAG", "Underexposed : ${abs(resultArray[0][2].toInt())}")

        val overExposurePercentage = resultArray[0][0] / 255
        val underExposurePercentage = resultArray[0][2] / 255

        if (overExposurePercentage > 0.6f) {
            // overexposed
            overExposeImageList.add(image)
        } else if (underExposurePercentage > 0.6f) {
            // underexposed
            underExposeImageList.add(image)
        } else {
            // good image
            goodImageList.add(image)
        }
    }

    // any coroutines launched inside this scope will run on the main thread unless stated otherwise
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        imageList.forEachIndexed { index, image ->
            uiScope.launch {
                processImage(image, index)
            }
        }
        startResultActivity()
        finish()
    }

    private fun startResultActivity() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }
}