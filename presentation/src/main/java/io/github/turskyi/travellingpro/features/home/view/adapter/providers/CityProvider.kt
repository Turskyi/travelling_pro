package io.github.turskyi.travellingpro.features.home.view.adapter.providers

import android.content.Intent
import android.net.Uri
import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.models.City

class CityProvider : BaseNodeProvider() {

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_city_content

    var onCityLongClickListener: ((data: City) -> Unit)? = null

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val entity: City = item as City
        helper.setText(R.id.tvCity, entity.name)
        helper.setText(R.id.tvMonth, entity.month)
    }

    override fun onClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ) {
        sendToGoogleMapToShow(data)
    }

    private fun sendToGoogleMapToShow(data: BaseNode) {
        val entity: City = data as City
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=${entity.name}")
        )
        context.startActivity(intent)
    }

    override fun onLongClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ): Boolean {
        val entity: City = data as City
        onCityLongClickListener?.invoke(entity)
        return true
    }
}