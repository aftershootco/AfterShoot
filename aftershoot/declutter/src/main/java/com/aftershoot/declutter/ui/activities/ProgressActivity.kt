package com.aftershoot.declutter.ui.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.R
import com.aftershoot.declutter.ui.activities.MainActivity.Companion.imageList
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.activity_progress.*

class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        LoaderTask().execute(imageList.size)
    }

    fun setupAnimation(){
        val animation = findViewById<LottieAnimationView>(R.id.progressBar)
        animation.speed = 2.0F // How fast does the animation play
        animation.progress = 50F // Starts the animation from 50% of the beginning
        animation.addAnimatorUpdateListener {
            // Called everytime the frame of the animation changes
        }
        animation.repeatMode = LottieDrawable.RESTART // Restarts the animation (you can choose to reverse it as well)
        animation.cancelAnimation() // Cancels the animation
    }

    inner class LoaderTask : AsyncTask<Int, Int, Unit>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Int?) {

            //Hardcoded for now, will add the model processing here
            for (i in 1..p0[0]!!) {
                Thread.sleep(50)
                publishProgress(i)
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