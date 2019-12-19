package com.aftershoot.declutter.ui

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_grid.view.*
import kotlinx.android.synthetic.main.layout_image_popup_alert.view.*
import java.io.File


class ResultImageAdapter(private var images: ArrayList<Image>, val activity: AppCompatActivity) :
        RecyclerView.Adapter<ResultImageAdapter.ImageHolder>() {

    private val selectedItems = arrayListOf<Image>()
    private var multiSelect = false

    // Outline what happens when the selection is started
    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // We have started multi selection, so set the flag to true
            multiSelect = true
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.menu_result, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false // Return false if nothing is done
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            if (item?.itemId == R.id.action_delete) {
                // Assuming that the delete button is clicked, finish the multi select process
                mode?.finish()
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // finished multi selection
            multiSelect = false
            notifyDataSetChanged()
        }
    }

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

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        context = parent.context
        return ImageHolder(layoutInflater.inflate(R.layout.item_grid, parent, false))
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        // for every item, check to see if it exists in the selected items array
        if (selectedItems.contains(images[position])) {
            holder.itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }

        Glide.with(context)
                .load(images[position].file)
                .into(holder.itemView.ivGrid)
    }

    fun selectItem(holder: ImageHolder, image: Image) {
        if (multiSelect) {
            if (selectedItems.contains(image)) {
                selectedItems.remove(image)
                holder.itemView.setBackgroundColor(Color.WHITE)
            } else {
                selectedItems.add(image)
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            }
        }
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.ivGrid.setOnClickListener {
                showPopup(images[adapterPosition].file)
            }

            // set handler to define what happens when an item is long pressed
            itemView.ivGrid.setOnLongClickListener {

                itemView.ivGrid.setOnClickListener {
                    selectItem(this, images[adapterPosition])
                }
                activity.startSupportActionMode(actionModeCallback)
                selectItem(this, images[adapterPosition])
                true
            }
        }
    }

    private fun showPopup(file: File) {
        dialogView.ivPopup.setImageURI(Uri.fromFile(file))
        alertDialog.show()
    }

    fun updateData(newPics: ArrayList<Image>) {
        images = newPics
        notifyDataSetChanged()
    }
}