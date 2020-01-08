package com.aftershoot.declutter.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.aftershoot.declutter.db.AfterShootDatabase
import com.aftershoot.declutter.db.Image

class ImagesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao by lazy {
        requireNotNull(AfterShootDatabase.getDatabase(application.applicationContext)?.getDao())
    }

    val blurredImageList: LiveData<List<Image>> by lazy {
        dao.getBlurredImages()
    }
    val overExposeImageList: LiveData<List<Image>> by lazy {
        dao.getOverExposedImages()
    }
    val underExposeImageList: LiveData<List<Image>> by lazy {
        dao.getUnderExposedImages()
    }
    val blinkImageList: LiveData<List<Image>> by lazy {
        dao.getBlinkImages()
    }
    val croppedImageList: LiveData<List<Image>> by lazy {
        dao.getCroppedFaceImages()
    }
    val goodImageList: LiveData<List<Image>> by lazy {
        dao.getGoodImages()
    }
}