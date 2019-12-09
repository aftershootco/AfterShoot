package com.aftershoot.declutter.ui

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_grid.view.*
import kotlinx.android.synthetic.main.layout_image_popup_alert.view.*
import java.io.File
import java.io.FileDescriptor
import java.io.FileNotFoundException


class ResultImageAdapter(private var images: ArrayList<Image>) :
        RecyclerView.Adapter<ResultImageAdapter.ImageHolder>() {

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }

    private val applicationContext by lazy {
        context.applicationContext
    }

    private val dialogView by lazy {
        layoutInflater.inflate(R.layout.layout_image_popup_alert, null, false)
    }

    private val alertDialog by lazy {
        AlertDialog.Builder(context)
                .setView(dialogView)
                .setOnCancelListener {

                    it.dismiss()
                }
                .create()
    }

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        context = parent.context
        return ImageHolder(layoutInflater.inflate(R.layout.item_grid, parent, false))
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        Glide.with(context)
                .load(images[position].file)
                .into(holder.itemView.ivGrid)
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)!!
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor: ParcelFileDescriptor = applicationContext.contentResolver.openFileDescriptor(uri, "r")!!
            val fileDescriptor: FileDescriptor = parcelFileDescriptor.getFileDescriptor()
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (fnfe: FileNotFoundException) {
            fnfe.printStackTrace()
            return null
        }
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.ivGrid.setOnClickListener {
                showPopup(images[adapterPosition].file)
            }
        }
    }

    private fun showPopup(file: File) {
        Glide.with(context)
                .load(file)
                .into(dialogView.ivPopup)
//        dialogView.ivPopup.setImageURI(uri)
        alertDialog.show()
    }

    fun updateData(newPics: ArrayList<Image>) {
        images = newPics
        notifyDataSetChanged()
    }
}