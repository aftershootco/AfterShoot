package com.aftershoot.declutter.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aftershoot.declutter.R
import com.aftershoot.declutter.db.AfterShootDatabase
import com.aftershoot.declutter.db.Image
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
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
import kotlin.math.absoluteValue

class ModelRunnerService : Service() {

    companion object {
        // Our model expects a RGB image, hence the channel size is 3
        private const val channelSize = 3

        // Width of the image that our model expects
        private const val inputImageWidth = 224
        // Height of the image that our model expects
        private const val inputImageHeight = 224
        // Size of the input buffer size (if your model expects a float input, multiply this with 4)
        private var modelInputSize = inputImageWidth * inputImageHeight * channelSize

        const val notificationId = 12345

        var isRunning = false
    }

    private val notification by lazy {
        NotificationCompat.Builder(this, "progress_channel")
                .setSmallIcon(R.drawable.ic_progress)
                .setContentTitle("Processing your images ...")
                .setProgress(100, 0, true)
                .build()
    }

    // for android version Oreo and above, we first need to create a notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // you can create multiple channels and deliver different type of notifications through different channels
            val notificationChannel = NotificationChannel("progress_channel", "Progress", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val dao by lazy {
        requireNotNull(AfterShootDatabase.getDatabase(this)?.getDao())
    }

    private val interpreterOptions by lazy {
        // use the neuralNet API if available
        Interpreter.Options()
                .setUseNNAPI(true)
    }

    private val exposureInterpreter by lazy {
        Interpreter(loadModelFile("exposure.tflite"), interpreterOptions)
    }

    private val blurInterpreter by lazy {
        Interpreter(loadModelFile("blur.tflite"), interpreterOptions)
    }

    // called when an instance of this service is created, if the service is already running; onStartCommand is called instead
    override fun onCreate() {
        super.onCreate()
        Log.e("TAG", "onCreate Called")
        isRunning = true
        createNotificationChannel()
        startForeground(notificationId, notification)
        CoroutineScope(Dispatchers.IO).launch {
            // get the unprocessed images only
            val images = dao.getUnprocessedImage()
            val totalImages = images.size
            CoroutineScope(Dispatchers.Main).launch {
                images.forEachIndexed { index, image ->
                    // set the progressBar to the current Progress
                    val notification = NotificationCompat.Builder(baseContext, "progress_channel")
                            .setSmallIcon(R.drawable.ic_progress)
                            .setContentTitle("Processing image : $index out of ${totalImages - 1}")
                            .setProgress(totalImages, index, false)
                            // setting the notification as ongoing prevents the user from dismissing it
                            .setOngoing(true)
                            .build()
                    // show a new notification with the same ID, it essentially cancels the older one and shows the new notification
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(notificationId, notification)

                    processImage(image)
                }
                // stop the foreground service and remove notification
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private suspend fun processImage(image: Image) = withContext(Dispatchers.IO) {
        val inputStream = contentResolver.openInputStream(image.uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        // Resize the bitmap so that it's 224x224
        bitmap?.let {
            val resizedImage =
                    Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)

            // Convert the bitmap to a ByteBuffer
            val modelInput = convertBitmapToByteBuffer(resizedImage)
            bitmap.recycle()
//            blurInference(modelInput, image)
            exposureInference(modelInput, image)
            blinkInference(image)
            dao.markProcessed(image.uri)
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
            dao.markBlurred(image.uri)
        }
    }

    private var highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

    private val detector by lazy {
        FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts)
    }

    private fun blinkInference(image: Image) {
        val visionImage = FirebaseVisionImage.fromFilePath(this, image.uri)
        detector.detectInImage(visionImage)
                .addOnSuccessListener {
                    it.forEach { face ->
                        // if the image contains even a single face with a closed eye, mark the image as blinked
                        if (face.leftEyeOpenProbability < 0.4 || face.rightEyeOpenProbability < 0.4 && face.smilingProbability < 0.3) {
                            CoroutineScope(Dispatchers.IO).launch {
                                dao.markBlink(image.uri)
                            }
                            return@forEach
                        }
                    }
                }
    }

    private fun exposureInference(buffer: ByteBuffer, image: Image) {
        // Output you get from your model, this is essentially as we saw in netron
        val resultArray = Array(1) { ByteArray(3) }

        exposureInterpreter.run(buffer, resultArray)

        val overExposurePercentage = (resultArray[0][0] / 255f).absoluteValue
        val underExposurePercentage = (resultArray[0][2] / 255f).absoluteValue

        if (overExposurePercentage > 0.45f) {
            // overexposed
            Log.e("TAG", "OverExposed")
            dao.markOverExposed(image.uri)
        } else if (underExposurePercentage > 0.45f) {
            // underexposed
            Log.e("TAG", "UnderExposed")
            dao.markUnderExposed(image.uri)
        }
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

    // used to communicate between service and the activity, skip for now
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        Log.e("TAG", "onDestroy called")
        super.onDestroy()
    }

}