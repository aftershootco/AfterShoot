package com.aftershoot.declutter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aftershoot.declutter.MainActivity.Companion.imageList
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_result_good.*


class GoodImageFragment : Fragment() {

    private val selections = arrayOf("Blurred", "Over Exposed", "Under Exposed", "Blinks")

    val imageAdapter by lazy {
        ResultImageAdapter(imageList)
    }

    lateinit var alertDialog: AlertDialog

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result_good, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvGoodPics.layoutManager = layoutManager
        rvGoodPics.adapter = imageAdapter
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        val callback = SimpleTouchHelperCallback(toDoAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rvGoodPics)
        rvGoodPics.smoothScrollToPosition(0)
    }

    private val toDoAdapter = object : ItemTouchHelperAdapter {

        override fun onDismiss(position: Int) {
            alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Mark this picture as")
                    .setItems(selections) { _, which ->
                        Snackbar.make(
                                container,
                                "Marked as a ${selections[which]} picture",
                                Snackbar.LENGTH_SHORT
                        ).show()
                        imageAdapter.notifyItemRemoved(position)
                        alertDialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
        }
    }

}