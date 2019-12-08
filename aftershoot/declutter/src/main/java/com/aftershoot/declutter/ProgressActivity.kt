package com.aftershoot.declutter

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.MainActivity.Companion.imageList
import kotlinx.android.synthetic.main.activity_progress.*

class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        LoaderTask().execute(imageList.size)
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