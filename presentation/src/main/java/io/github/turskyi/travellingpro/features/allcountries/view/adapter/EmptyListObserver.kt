package io.github.turskyi.travellingpro.features.allcountries.view.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.turskyi.travellingpro.extensions.setDynamicVisibility

class EmptyListObserver(private val recyclerView: RecyclerView, private val emptyView: View?) :
    RecyclerView.AdapterDataObserver() {

    init {
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        emptyView?.setDynamicVisibility(recyclerView.adapter?.itemCount == 0)
    }

    override fun onChanged() = checkIfEmpty()
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkIfEmpty()
    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkIfEmpty()
}