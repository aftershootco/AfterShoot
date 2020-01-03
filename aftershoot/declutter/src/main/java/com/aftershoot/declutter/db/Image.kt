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
        var isBlurred: Boolean = false,
        var isOverExposed: Boolean = false,
        var isUnderExposed: Boolean = false,
        var isBlink: Boolean = false,
        var isCroppedFace: Boolean = false,
        var processed: Boolean = false)
