package io.github.turskyi.travellingpro.features.home.view.adapter.providers

import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.models.VisitedCountryNode

class CountryNodeProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = 0

    override val layoutId: Int
        get() = R.layout.item_list_country

    var onImageClickListener: ((data: VisitedCountryNode) -> Unit)? = null
    var onTextClickListener: ((data: VisitedCountryNode) -> Unit)? = null
    var onLongLickListener: ((data: VisitedCountryNode) -> Unit)? = null

    override fun convert(
        helper: BaseViewHolder,
        item: BaseNode
    ) {
        val visitedCountryNode: VisitedCountryNode = item as VisitedCountryNode
        helper.getView<TextView>(R.id.tv_country).setOnLongClickListener {
            onLongLickListener?.invoke(visitedCountryNode)
            true
        }
        showPicturesInSVG(item, helper)
        helper.setText(R.id.tv_country, visitedCountryNode.title)
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.iv_flag))
        helper.getView<ImageView>(R.id.iv_flag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountryNode)
        }
        setSelectableBorderLessFor(helper.getView<WebView>(R.id.wv_flag))
        helper.getView<WebView>(R.id.wv_flag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountryNode)
        }
        setSelectableBackgroundFor(helper.getView<TextView>(R.id.tv_country))
        helper.getView<TextView>(R.id.tv_country).setOnClickListener {
            onTextClickListener?.invoke(visitedCountryNode)
        }
        if (item.childNode.isNullOrEmpty()) {
            helper.setVisible(R.id.iv_more, false)
        } else {
            if (visitedCountryNode.isExpanded) {
                helper.setImageResource(
                    R.id.iv_more,
                    R.drawable.ic_arrow_expandable_up
                )
            } else {
                helper.setImageResource(
                    R.id.iv_more,
                    R.drawable.ic_arrow_expandable_down
                )
            }
            helper.setVisible(R.id.iv_more, true)
        }
    }

    private fun setSelectableBackgroundFor(it: View) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
        it.setBackgroundResource(outValue.resourceId)
    }

    private fun setSelectableBorderLessFor(view: View) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            R.attr.selectableItemBackgroundBorderless, outValue,
            true
        )
        view.setBackgroundResource(outValue.resourceId)
    }

    private fun showPicturesInSVG(
        visitedCountryNode: VisitedCountryNode,
        holder: BaseViewHolder
    ) {
        val uri: Uri = Uri.parse(visitedCountryNode.flag)
        GlideToVectorYou
            .init()
            .with(holder.itemView.context)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() {
                    showPicturesInWebView(holder, visitedCountryNode)
                }

                override fun onResourceReady() {
                    holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = VISIBLE
                    holder.itemView.findViewById<WebView>(R.id.wv_flag).visibility = GONE
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, holder.itemView.findViewById(R.id.iv_flag))
    }

    private fun showPicturesInWebView(
        holder: BaseViewHolder,
        visitedCountryNode: VisitedCountryNode
    ) {
        holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = GONE
        val wvFlag = holder.itemView.findViewById<WebView>(R.id.wv_flag)
        wvFlag.apply {
            webViewClient = WebViewClient()
            visibility = VISIBLE
            setBackgroundColor(Color.TRANSPARENT)
            loadData(
                "<html><head><style type='text/css'>" +
                        "body{margin:auto auto;text-align:center;} img{width:80%25;}" +
                        " </style></head><body><img src='${visitedCountryNode.flag}'/>" +
                        "</body></html>", "text/html", "UTF-8"
            )
        }
    }

    override fun onClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ) {
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.iv_more))
        getAdapter()?.expandOrCollapse(position)
    }
}