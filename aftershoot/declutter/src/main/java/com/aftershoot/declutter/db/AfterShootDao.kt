package com.aftershoot.declutter.db

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AfterShootDao {
    @Query("SELECT * FROM afterShootImage")
    fun getAllImages(): LiveData<List<Image>>

    @Query("SELECT * FROM afterShootImage WHERE isBlurred = 1")
    fun getBlurredImages(): LiveData<List<Image>>

    @Query("SELECT * FROM afterShootImage WHERE isOverExposed = 1")
    fun getOverExposedImages(): LiveData<List<Image>>

    @Query("SELECT * FROM afterShootImage WHERE isUnderExposed = 1")
    fun getUnderExposedImages(): LiveData<List<Image>>

    @Query("SELECT * FROM afterShootImage WHERE isCroppedFace = 1")
    fun getCroppedFaceImages(): LiveData<List<Image>>

    @Query("SELECT * FROM afterShootImage WHERE isBlink = 1")
    fun getBlinkImages(): LiveData<List<Image>>

    @Query("SELECT * FROM afterShootImage WHERE isBlurred = 0 AND isBlink = 0 AND isCroppedFace = 0 AND isOverExposed = 0 AND isUnderExposed")
    fun getGoodImages(): LiveData<List<Image>>

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

    @Query("UPDATE afterShootImage SET isBlurred = 1 WHERE uri = :uri")
    fun markBlurred(uri: Uri)

    @Query("UPDATE afterShootImage SET isOverExposed = 1 WHERE uri = :uri")
    fun markOverExposed(uri: Uri)

    @Query("UPDATE afterShootImage SET isUnderExposed = 1 WHERE uri = :uri")
    fun markUnderExposed(uri: Uri)

    @Query("UPDATE afterShootImage SET isCroppedFace = 1 WHERE uri = :uri")
    fun markCroppedFaces(uri: Uri)

    @Query("UPDATE afterShootImage SET isBlink = 1 WHERE uri = :uri")
    fun markBlink(uri: Uri)

    // custom update method that only marks the processed column of the entry as true, leaving other fields as it is
    @Query("UPDATE afterShootImage SET processed = 1 WHERE uri = :uri")
    fun markProcessed(uri: Uri)

}