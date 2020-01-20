package com.aftershoot.declutter.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.aftershoot.declutter.R
import com.aftershoot.declutter.db.Image
import com.aftershoot.declutter.ui.activities.AdapterCallBack
import com.aftershoot.declutter.ui.activities.ImageActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import kotlinx.android.synthetic.main.item_grid.view.*

class ResultImageAdapter(private var images: List<Image>, private val callback: AdapterCallBack) :
        RecyclerView.Adapter<ResultImageAdapter.ImageHolder>(), RecyclerViewFastScroller.OnPopupTextUpdate {

    // true if the user in selection mode, false otherwise
    var multiSelect = false
    // Keeps track of all the selected images
    val selectedItems = arrayListOf<Image>()

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
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

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        context = parent.context
        return ImageHolder(layoutInflater.inflate(R.layout.item_grid, parent, false))
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        val currentImage = images[position]

        // for every item, check to see if it exists in the selected items array
        if (selectedItems.contains(currentImage)) {
            // if the item is selected, let the user know by adding a dark layer above it
            holder.itemView.ivGrid.alpha = 0.3f
        } else {
            // else, keep it as it is
            holder.itemView.ivGrid.alpha = 1.0f
        }

        holder.itemView.ivGrid.setOnClickListener {
            if (multiSelect)
                selectItem(holder, images[holder.adapterPosition])
            else
                callback.showImage(images[holder.adapterPosition].uri, holder)
        }

        // set handler to define what happens when an item is long pressed
        holder.itemView.ivGrid.setOnLongClickListener {
            if (!multiSelect) {
                // We have started multi selection, so set the flag to true
                multiSelect = true

                callback.onSelected()
                selectItem(holder, images[holder.adapterPosition])
                true
            } else
                false
        }

        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(320, 320)

        Glide.with(context)
                .load(currentImage.uri)
                .apply(requestOptions)
                .into(holder.itemView.ivGrid)
    }

    private fun selectItem(holder: ImageHolder, image: Image) {
        if (selectedItems.contains(image)) {
            selectedItems.remove(image)
            holder.itemView.ivGrid.alpha = 1.0f
        } else {
            selectedItems.add(image)
            holder.itemView.ivGrid.alpha = 0.3f
        }
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    fun updateData(newPics: List<Image>) {
        images = newPics
        notifyDataSetChanged()
    }

    fun backPressed() {
        selectedItems.clear()
        multiSelect = false
        notifyDataSetChanged()
    }

    override fun onChange(position: Int) = images[position].dateTaken.toString()

}