package com.aftershoot.declutter.ui.fragments

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aftershoot.declutter.R
import com.aftershoot.declutter.db.Image
import com.aftershoot.declutter.helper.ItemTouchHelperAdapter
import com.aftershoot.declutter.helper.SimpleTouchHelperCallback
import com.aftershoot.declutter.ui.ResultImageAdapter
import com.aftershoot.declutter.ui.activities.AdapterCallBack
import com.aftershoot.declutter.ui.activities.ImageActivity
import com.aftershoot.declutter.ui.viewmodels.ImagesViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_result_bad.*
import kotlinx.android.synthetic.main.item_grid.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BadImageFragment : Fragment(), AdapterCallBack {

    private val selections = arrayOf("Over Exposed", "Under Exposed", "Blinks", "Cropped Faces", "Blurred", "Good")
    var currentMode = selections[0]

    private lateinit var blurredImageList: LiveData<List<Image>>
    private lateinit var overExposeImageList: LiveData<List<Image>>
    private lateinit var underExposeImageList: LiveData<List<Image>>
    private lateinit var blinkImageList: LiveData<List<Image>>
    private lateinit var croppedImageList: LiveData<List<Image>>
    private lateinit var goodImageList: LiveData<List<Image>>

    private val imageModel by lazy {
        ViewModelProviders.of(this).get(ImagesViewModel::class.java)
    }

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

            val deleteAlert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete ${itemAdapter.selectedItems.size} images?")
                    .setMessage("This action can't be undone!")
                    .setCancelable(true)
                    .setPositiveButton("Yes") { dialog, _ ->
                        // TODO : Add deletion here
                        Toast.makeText(requireContext(), "${itemAdapter.selectedItems.size} images deleted", Toast.LENGTH_SHORT).show()
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
            itemAdapter.multiSelect = false
            itemAdapter.selectedItems.clear()
            itemAdapter.notifyDataSetChanged()
        }
    }

    private fun clearObservers() {
        blurredImageList.removeObserver(observer)
        overExposeImageList.removeObserver(observer)
        underExposeImageList.removeObserver(observer)
        blinkImageList.removeObserver(observer)
        croppedImageList.removeObserver(observer)
        goodImageList.removeObserver(observer)
    }

    private val observer by lazy {
        Observer<List<Image>> { images -> itemAdapter.updateData(requireNotNull(images)) }
    }

    private suspend fun initLists() = withContext(Dispatchers.IO) {
        blurredImageList = imageModel.blurredImageList
        blinkImageList = imageModel.blinkImageList
        underExposeImageList = imageModel.underExposeImageList
        overExposeImageList = imageModel.overExposeImageList
        croppedImageList = imageModel.croppedImageList
        goodImageList = imageModel.goodImageList
    }

    private lateinit var itemAdapter: ResultImageAdapter

    private val alertFilter: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
                .setTitle("Select the filter")
                .setItems(selections) { _, which ->
                    clearObservers()
                    when (which) {
                        0 -> {
                            overExposeImageList.observe(this, observer)
                            currentMode = selections[0]
                        }
                        1 -> {
                            underExposeImageList.observe(this, observer)
                            currentMode = selections[1]
                        }
                        2 -> {
                            blinkImageList.observe(this, observer)
                            currentMode = selections[2]
                        }
                        3 -> {
                            croppedImageList.observe(this, observer)
                            currentMode = selections[3]
                        }
                        4 -> {
                            blurredImageList.observe(this, observer)
                            currentMode = selections[4]
                        }
                        5 -> {
                            goodImageList.observe(this, observer)
                            currentMode = selections[5]
                        }
                    }
                    alertFilter.dismiss()
                }
                .create()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result_bad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            initLists()
            overExposeImageList.observe(viewLifecycleOwner, observer)
            itemAdapter = ResultImageAdapter(listOf(), requireActivity() as AppCompatActivity)
            fabFilter.setOnClickListener {
                alertFilter.show()
            }
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            rvBadPics.layoutManager = layoutManager
            rvBadPics.adapter = itemAdapter
            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            rvBadPics.smoothScrollToPosition(0)
            val callback = SimpleTouchHelperCallback(toDoAdapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(rvBadPics)
        }
    }

    private fun notifyItemRemoval(pos: Int) {
        itemAdapter.notifyItemRemoved(pos)
    }

    fun notifyBackPressed() {
        // Clear the selection from the adapter
        itemAdapter.backPressed()
    }

    private val toDoAdapter = object : ItemTouchHelperAdapter {

        override fun onDismiss(position: Int) {
            when (currentMode) {
                selections[0] -> {
//                    goodArray.add(blurredArray.removeAt(position))
                }
                selections[1] -> {
//                    goodArray.add(overExposedArray.removeAt(position))
                }
                selections[2] -> {
//                    goodArray.add(underExposedArray.removeAt(position))
                }
                selections[3] -> {
//                    goodArray.add(blinksArray.removeAt(position))
                }
            }
            Snackbar.make(fabFilter, "Marked as a good picture", Snackbar.LENGTH_SHORT).show()
            notifyItemRemoval(position)
        }
    }

    override fun onSelected() {
        (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
    }

    private val intent by lazy {
        Intent(context, ImageActivity::class.java)
    }

    override fun showImage(uri: Uri, holder: ResultImageAdapter.ImageHolder) {
        intent.putExtra("URI", uri.toString())
        val options = ActivityOptions
                .makeSceneTransitionAnimation(activity, holder.itemView.ivGrid, "image")
        requireContext().startActivity(intent, options.toBundle())
    }

}