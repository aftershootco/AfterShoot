package com.aftershoot.declutter.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aftershoot.declutter.R
import com.aftershoot.declutter.ui.fragments.BadImageFragment


class ResultActivity : AppCompatActivity() {

    private val badImageFragment by lazy {
        BadImageFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        checkBattery()
        supportFragmentManager.beginTransaction().add(R.id.container, badImageFragment).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        badImageFragment.notifyBackPressed()
    }

    private fun isBatteryOptimized(): Boolean {
        val pwrm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !pwrm.isIgnoringBatteryOptimizations(name)
        }
        return false
    }

    private fun checkBattery() {
        if (isBatteryOptimized() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val name = resources.getString(R.string.app_name)

            AlertDialog.Builder(this)
                    .setTitle("Please Disable Battery optimizations")
                    .setMessage("Let AfterShoot perform image processing in the background.\n\nIn the next screen, select 'All Apps' from the drop-down menu and uncheck 'DeClutter by Aftershoot'")
                    .setCancelable(false)
                    .setNeutralButton("Ok") { dialog, which ->
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                    }
                    .show()
        }
    }

}