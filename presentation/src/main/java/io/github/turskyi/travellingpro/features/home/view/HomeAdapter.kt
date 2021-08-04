package io.github.turskyi.travellingpro.features.home.view

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import io.github.turskyi.travellingpro.common.view.providers.CityProvider
import io.github.turskyi.travellingpro.common.view.providers.CountryNodeProvider
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.VisitedCountryNode

class HomeAdapter : BaseNodeAdapter() {

    private var countryNodeProvider = CountryNodeProvider()
    private var cityProvider = CityProvider()

    init {
        addFullSpanNodeProvider(countryNodeProvider)
        addNodeProvider(cityProvider)
    }

    var onFlagClickListener: ((data: VisitedCountryNode) -> Unit)? = null
        set(value) {
            countryNodeProvider.onImageClickListener = value
            field = value
        }

    var onCountryNameClickListener: ((data: VisitedCountryNode) -> Unit)? = null
        set(value) {
            countryNodeProvider.onTextClickListener = value
            field = value
        }

    var onLongClickListener: ((data: VisitedCountryNode) -> Unit)? = null
        set(value) {
            countryNodeProvider.onLongLickListener = value
            field = value
        }

    var onCityLongClickListener: ((data: City) -> Unit)? = null
        set(value) {
            cityProvider.onCityLongClickListener = value
            field = value
        }

    override fun getItemType(
        data: List<BaseNode>,
        position: Int
    ) = when (data[position]) {
            is VisitedCountryNode -> 0
            is City -> 1
            else -> -1
        }
}