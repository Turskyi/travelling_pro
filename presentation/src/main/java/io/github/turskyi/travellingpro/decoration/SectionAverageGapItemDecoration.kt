package io.github.turskyi.travellingpro.decoration

import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.entity.SectionEntity
import java.util.*

class SectionAverageGapItemDecoration(
    private val gapHorizontalDp: Int,
    private val gapVerticalDp: Int,
    private val sectionEdgeHPaddingDp: Int,
    private val sectionEdgeVPaddingDp: Int
) : ItemDecoration() {
    private inner class Section {
        var startPos = 0
        var endPos = 0
        val count: Int
            get() = endPos - startPos + 1

        operator fun contains(pos: Int): Boolean {
            return pos in startPos..endPos
        }

        override fun toString(): String {
            return "Section{" +
                    "startPos=" + startPos +
                    ", endPos=" + endPos +
                    '}'
        }
    }

    private var gapHSizePx = -1
    private var gapVSizePx = -1
    private var sectionEdgeHPaddingPx = 0
    private var eachItemHPaddingPx = 0
    private var sectionEdgeVPaddingPx = 0
    private val mSectionList: MutableList<Section?> = ArrayList()
    private lateinit var mAdapter: BaseSectionQuickAdapter<*, *>
    private val mDataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() = markSections()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = markSections()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
            markSections()

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = markSections()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = markSections()
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
            markSections()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.layoutManager is GridLayoutManager && parent.adapter is BaseSectionQuickAdapter<*, *>) {
            val layoutManager = parent.layoutManager as GridLayoutManager?
            val adapter: BaseSectionQuickAdapter<*, *>? =
                parent.adapter as BaseSectionQuickAdapter<*, *>?
            if (mAdapter !== adapter) setUpWithAdapter(adapter)
            val spanCount = layoutManager?.spanCount
            val position = parent.getChildAdapterPosition(view) - mAdapter.headerLayoutCount
            val entity = adapter?.getItem(position)
            entity?.let {
                if (entity.isHeader) {
                    /*header*/
                    outRect[0, 0, 0] = 0
                    return
                }
            }
            val section = findSectionLastItemPos(position)
            if (gapHSizePx < 0 || gapVSizePx < 0) spanCount?.let { spanCountNum ->
                transformGapDefinition(spanCountNum)
            }
            outRect.top = gapVSizePx
            outRect.bottom = 0

            /* visualPos Section */
            val visualPos = position + 1 - section!!.startPos
            when {
                visualPos % spanCount!! == 1 -> {
                    outRect.left = sectionEdgeHPaddingPx
                    outRect.right = eachItemHPaddingPx - sectionEdgeHPaddingPx
                }
                visualPos % spanCount == 0 -> {
                    outRect.left = eachItemHPaddingPx - sectionEdgeHPaddingPx
                    outRect.right = sectionEdgeHPaddingPx
                }
                else -> {
                    outRect.left = gapHSizePx - (eachItemHPaddingPx - sectionEdgeHPaddingPx)
                    outRect.right = eachItemHPaddingPx - outRect.left
                }
            }
            if (visualPos - spanCount <= 0) outRect.top = sectionEdgeVPaddingPx
            if (isLastRow(visualPos, spanCount, section.count)) {
                outRect.bottom = sectionEdgeVPaddingPx
            }
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }

    private fun setUpWithAdapter(adapter: BaseSectionQuickAdapter<*, *>?) {
        mAdapter.unregisterAdapterDataObserver(mDataObserver)
        adapter?.let{ mAdapter = adapter }
        mAdapter.registerAdapterDataObserver(mDataObserver)
        markSections()
    }

    private fun markSections() {
        val adapter: BaseSectionQuickAdapter<*, *> = mAdapter
        mSectionList.clear()
        var sectionEntity: SectionEntity?
        var section = Section()
        var i = 0
        val size = adapter.itemCount
        while (i < size) {
            sectionEntity = adapter.getItem(i)
            if (sectionEntity.isHeader) {
                if (i != 0) {
             /*       section */
                    section.endPos = i - 1
                    mSectionList.add(section)
                }
                section = Section()
                section.startPos = i + 1
            } else {
                section.endPos = i
            }
            i++
        }
        if (!mSectionList.contains(section)) mSectionList.add(section)
    }

    private fun transformGapDefinition(spanCount: Int) {
        val displayMetrics = DisplayMetrics()
        gapHSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            gapHorizontalDp.toFloat(),
            displayMetrics
        ).toInt()
        gapVSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            gapVerticalDp.toFloat(),
            displayMetrics
        ).toInt()
        sectionEdgeHPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sectionEdgeHPaddingDp.toFloat(),
            displayMetrics
        ).toInt()
        sectionEdgeVPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sectionEdgeVPaddingDp.toFloat(),
            displayMetrics
        ).toInt()
        eachItemHPaddingPx = (sectionEdgeHPaddingPx * 2 + gapHSizePx * (spanCount - 1)) / spanCount
    }

    private fun findSectionLastItemPos(curPos: Int): Section? {
        for (section in mSectionList) {
            section?.contains(curPos)?.let{
                if (section.contains(curPos)) {
                    return section
                }
            }
        }
        return null
    }

    private fun isLastRow(visualPos: Int, spanCount: Int, sectionItemCount: Int): Boolean {
        var lastRowCount = sectionItemCount % spanCount
        lastRowCount = if (lastRowCount == 0) spanCount else lastRowCount
        return visualPos > sectionItemCount - lastRowCount
    }
}