package com.aftershoot.declutter.db

import android.net.Uri
import androidx.room.*

@Dao
interface AfterShootDao {
    @Query("SELECT * FROM afterShootImage")
    fun getAllImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE isBlurred = 1")
    fun getBlurredImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE isOverExposed = 1")
    fun getOverExposedImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE isUnderExposed = 1")
    fun getUnderExposedImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE isCroppedFace = 1")
    fun getCroppedFaceImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE isBlink = 1")
    fun getBlinkImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE isBlurred = 0 AND isBlink = 0 AND isCroppedFace = 0 AND isOverExposed = 0 AND isUnderExposed")
    fun getGoodImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE processed = 0")
    fun getUnprocessedImage(): List<Image>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insetImage(image: Image)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMultipleImages(images: List<Image>)

    @Delete
    fun deleteImage(image: Image)

    @Query("DELETE FROM afterShootImage")
    fun cleanDatabase()

    @Update
    fun updateImage(image: Image)

    // custom update method that only marks the processed column of the entry as true, leaving other fields as it is
    @Query("UPDATE afterShootImage SET processed = 1 WHERE uri = :uri")
    fun markProcessed(uri: Uri)

}