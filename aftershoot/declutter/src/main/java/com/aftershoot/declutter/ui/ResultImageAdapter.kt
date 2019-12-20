package com.aftershoot.declutter.ui

import android.content.Context
import android.net.Uri
import android.view.*
import android.widget.Toast
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

            val deleteAlert = AlertDialog.Builder(context)
                    .setTitle("Delete ${selectedItems.size} images?")
                    .setMessage("This action can't be undone!")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { dialog, _ ->
                        // TODO : Add deletion here
                        Toast.makeText(context, "${selectedItems.size} images deleted", Toast.LENGTH_SHORT).show()
                        selectedItems.clear()
                        dialog.dismiss()
                        mode?.finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

            if (item?.itemId == R.id.action_delete) {
                // Assuming that the delete button is clicked, handle deletion and finish the multi select process
                deleteAlert.show()
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
                showPopup(images[holder.adapterPosition].file)
        }

        // set handler to define what happens when an item is long pressed
        holder.itemView.ivGrid.setOnLongClickListener {
            if (!multiSelect) {
                activity.startSupportActionMode(actionModeCallback)
                selectItem(holder, images[holder.adapterPosition])
            }
            true
        }

        Glide.with(context)
                .load(images[position].file)
                .into(holder.itemView.ivGrid)
    }

    private fun selectItem(holder: ImageHolder, image: Image) {
        if (multiSelect) {
            if (selectedItems.contains(image)) {
                selectedItems.remove(image)
                holder.itemView.ivGrid.alpha = 1.0f
            } else {
                selectedItems.add(image)
                holder.itemView.ivGrid.alpha = 0.3f
            }
        }
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun showPopup(file: File) {
        dialogView.ivPopup.setImageURI(Uri.fromFile(file))
        alertDialog.show()
    }

    fun updateData(newPics: ArrayList<Image>) {
        images = newPics
        notifyDataSetChanged()
    }
}