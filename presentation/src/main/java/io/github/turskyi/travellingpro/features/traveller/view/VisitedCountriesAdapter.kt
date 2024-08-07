package io.github.turskyi.travellingpro.features.traveller.view

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import io.github.turskyi.travellingpro.common.view.providers.CityProvider
import io.github.turskyi.travellingpro.common.view.providers.CountryNodeProvider
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.VisitedCountryNode

class VisitedCountriesAdapter : BaseNodeAdapter() {

    private val countryNodeProvider = CountryNodeProvider()
    private val cityProvider = CityProvider()

    init {
        addFullSpanNodeProvider(countryNodeProvider)
        addNodeProvider(cityProvider)
    }

    var onFlagClickListener: ((data: VisitedCountryNode) -> Unit)? = null
        set(value) {
            countryNodeProvider.onImageClickListener = value
            field = value
        }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is VisitedCountryNode -> 0
            is City -> 1
            else -> -1
        }
    }
}