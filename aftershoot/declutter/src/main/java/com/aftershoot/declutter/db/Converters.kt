package com.aftershoot.declutter.db

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun stringToUri(uri: String): Uri = Uri.parse(uri)

    @TypeConverter
    fun uriToString(uri: Uri) = uri.toString()

    @TypeConverter
    fun fromString(value: String): ArrayList<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromListList(list: ArrayList<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

}