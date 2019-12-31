package com.aftershoot.declutter.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "afterShootImage")
data class Image(
        @PrimaryKey
        val uri: Uri,
        val name: String,
        val size: String,
        val dateTaken: String?,
        val issues: ArrayList<String> = arrayListOf(),
        var processed: Boolean = false)


const val BLUR = "blur"
const val OVER_EXPOSED = "over_exposure"
const val UNDER_EXPOSED = "under_exposure"
const val BLINK = "blink"
const val CROPPED_FACE = "cropped_face"
