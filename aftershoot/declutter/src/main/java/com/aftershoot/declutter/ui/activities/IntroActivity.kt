package com.aftershoot.declutter.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aftershoot.declutter.R
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage

class IntroActivity : AppIntro() {

    private val RQ_PERMISSION = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sliderOne = SliderPage().apply {
            title = "DeClutter"
            description = "An app to help manage your storage"
            imageDrawable = R.drawable.logo_horiz
            bgColor = ContextCompat.getColor(baseContext, R.color.primaryColor)

        }

        addSlide(AppIntroFragment.newInstance(sliderOne))

        val sliderTwo = SliderPage().apply {
            title = "DeClutter"
            description = "In order for the app to work, please accept the storage permissions next!"
            imageDrawable = R.drawable.logo_horiz
            bgColor = ContextCompat.getColor(baseContext, R.color.primaryColor)
        }


        addSlide(AppIntroFragment.newInstance(sliderTwo))

        showSkipButton(false)
    }

    private fun requestPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), RQ_PERMISSION)
        }

    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RQ_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else {
            Toast.makeText(this, "Please accept the storage permissions for the app to work!", Toast.LENGTH_SHORT).show()
            requestPermissions()
        }
    }

}