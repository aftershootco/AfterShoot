package com.aftershoot.declutter.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aftershoot.declutter.R
import com.aftershoot.declutter.db.*
import com.aftershoot.declutter.helper.ItemTouchHelperAdapter
import com.aftershoot.declutter.helper.SimpleTouchHelperCallback
import com.aftershoot.declutter.ui.ResultImageAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_result_bad.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BadImageFragment : Fragment() {

    private val selections = arrayOf("All", "Blurred", "Over Exposed", "Under Exposed", "Blinks", "Cropped Faces")
    var currentMode = selections[0]

    private val dao by lazy {
        AfterShootDatabase.getDatabase(requireContext())?.getDao()!!
    }

    private lateinit var allImageList: List<Image>
    private lateinit var blurredImageList: List<Image>
    private lateinit var overExposeImageList: List<Image>
    private lateinit var underExposeImageList: List<Image>
    private lateinit var blinkImageList: List<Image>
    private lateinit var croppedImageList: List<Image>

    private suspend fun initLists() = withContext(Dispatchers.IO) {
        blinkImageList = dao.getBadImage(BLINK)
        underExposeImageList = dao.getBadImage(UNDER_EXPOSED)
        overExposeImageList = dao.getBadImage(OVER_EXPOSED)
        blurredImageList = dao.getBadImage(BLUR)
        croppedImageList = dao.getBadImage(CROPPED_FACE)
        allImageList = dao.getAllImages()
    }

    private lateinit var itemAdapter: ResultImageAdapter

    private val alertFilter: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
                .setTitle("Select the filter")
                .setItems(selections) { _, which ->
                    when (which) {
                        0 -> {
                            updateAdapter(allImageList)
                            currentMode = selections[0]
                        }
                        1 -> {
                            updateAdapter(blurredImageList)
                            currentMode = selections[1]
                        }
                        2 -> {
                            updateAdapter(overExposeImageList)
                            currentMode = selections[2]
                        }
                        3 -> {
                            updateAdapter(underExposeImageList)
                            currentMode = selections[3]
                        }
                        4 -> {
                            updateAdapter(blinkImageList)
                            currentMode = selections[4]
                        }
                        5 -> {
                            updateAdapter(croppedImageList)
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
            Log.e("TAG", "BadImg Fragment ${allImageList.size}")
            itemAdapter = ResultImageAdapter(allImageList, requireActivity() as AppCompatActivity)
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

    private fun updateAdapter(newPics: List<Image>) {
        itemAdapter.updateData(newPics)
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

}