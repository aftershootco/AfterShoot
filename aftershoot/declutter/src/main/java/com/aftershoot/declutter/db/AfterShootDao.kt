package com.aftershoot.declutter.db

import androidx.room.*

@Dao
interface AfterShootDao {
    @Query("SELECT * FROM afterShootImage")
    fun getAllImages(): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE issues LIKE '%' || :issue || '%' ")
    fun getBadImage(issue: String): List<Image>

    @Query("SELECT * FROM afterShootImage WHERE processed = 0")
    fun getUnprocessedImage(): List<Image>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insetImage(image: Image)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleImages(images: List<Image>)

    @Delete
    fun deleteImage(image: Image)

    @Query("DELETE FROM afterShootImage")
    fun cleanDatabase()

    @Update
    fun updateImage(image: Image)
}