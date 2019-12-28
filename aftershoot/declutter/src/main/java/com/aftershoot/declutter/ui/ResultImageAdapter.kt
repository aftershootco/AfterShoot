package com.aftershoot.declutter.ui

import android.content.Context
import android.net.Uri
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.aftershoot.declutter.R
import com.aftershoot.declutter.model.Image
import kotlinx.android.synthetic.main.item_grid.view.*
import kotlinx.android.synthetic.main.layout_image_popup_alert.view.*

class ResultImageAdapter(private var images: ArrayList<Image>, private val activity: AppCompatActivity) :
        RecyclerView.Adapter<ResultImageAdapter.ImageHolder>() {

    // true if the user in selection mode, false otherwise
    private var multiSelect = false
    // Keeps track of all the selected images
    private val selectedItems = arrayListOf<Image>()

    // Outline what happens when the selection is started
    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
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
                        mode?.finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        mode?.finish()
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
            selectedItems.clear()
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
                showPopup(images[holder.adapterPosition].uri)
        }

        // set handler to define what happens when an item is long pressed
        holder.itemView.ivGrid.setOnLongClickListener {
            if (!multiSelect) {
                // We have started multi selection, so set the flag to true
                multiSelect = true
                activity.startSupportActionMode(actionModeCallback)
                selectItem(holder, images[holder.adapterPosition])
                true
            } else
                false
        }

        //Don't load the entire image, to save memory
        val thumbnail = context.contentResolver.loadThumbnail(currentImage.uri, Size(240, 240), null)
        holder.itemView.ivGrid.setImageBitmap(thumbnail)
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

    private fun showPopup(uri: Uri) {
        dialogView.ivPopup.setImageURI(uri)
        alertDialog.show()
    }

    fun updateData(newPics: ArrayList<Image>) {
        images = newPics
        notifyDataSetChanged()
    }

    fun backPressed() {
        selectedItems.clear()
        multiSelect = false
        notifyDataSetChanged()
    }

}