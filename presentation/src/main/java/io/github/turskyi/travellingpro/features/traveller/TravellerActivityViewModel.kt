package io.github.turskyi.travellingpro.features.traveller

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import io.github.turskyi.travellingpro.features.traveller.view.TravellerActivity
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToBaseNodeList
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedListToVisitedNodeList
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedModelListToVisitedList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TravellerActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    var notVisitedCountriesCount: Float = 0F
    var citiesCount: Int = 0
    var mLastClickTime: Long = 0

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries: MutableLiveData<List<VisitedCountry>> =
        MutableLiveData<List<VisitedCountry>>()
    val visitedCountries: LiveData<List<VisitedCountry>>
        get() = _visitedCountries

    private val _visitedCountriesWithCitiesNode: MutableLiveData<List<VisitedCountryNode>> =
        MutableLiveData<List<VisitedCountryNode>>()
    val visitedCountriesWithCitiesNode: LiveData<List<VisitedCountryNode>>
        get() = _visitedCountriesWithCitiesNode

    private val _errorMessage: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage


    /** [showListOfVisitedCountriesById] is the first function in [TravellerActivity] */
    fun showListOfVisitedCountriesById(id: String) {
        _visibilityLoader.postValue(VISIBLE)
        runBlocking {
            viewModelScope.launch {
                // loading count of not visited countries
                interactor.setNotVisitedCountriesNum(
                    id = id,
                    onSuccess = { notVisitedCountriesNum ->
                        // loading visited countries
                        notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                    },
                    onError = { exception -> showError(exception) },
                )
            }
            setVisitedCountries(id)
        }
    }

    private fun setVisitedCountries(userId: String) {
        viewModelScope.launch {
            interactor.setVisitedCountries(
                id = userId,
                onSuccess = { countries ->
                    val visitedCountries: List<VisitedCountry> =
                        countries.mapVisitedModelListToVisitedList()

                    val visitedCountryWithCityNodes: MutableList<VisitedCountryNode> =
                        visitedCountries.mapVisitedListToVisitedNodeList()
                    if (visitedCountryWithCityNodes.isEmpty()) {
                        // if there are no countries there are no cities, show empty list
                        showVisitedCountryNodes(mutableListOf(), emptyList())
                    } else {
                        fillCountriesWithCities(
                            userId,
                            visitedCountryWithCityNodes,
                            visitedCountries
                        )
                        /* do not write any logic after  countries loop (here),
                         * rest of the logic must be in "get cities" success method ,
                         * since it started later then here */
                    }
                },
                onError = { exception -> showError(exception) },
            )
        }
    }

    /** filling country nodes with cities */
    private fun fillCountriesWithCities(
        userId: String,
        visitedCountryWithCityNodes: MutableList<VisitedCountryNode>,
        visitedCountries: List<VisitedCountry>
    ) {
        for (country in visitedCountryWithCityNodes) {
            val cityList: MutableList<BaseNode> = mutableListOf()
            viewModelScope.launch {
                interactor.setCities(
                    userId = userId,
                    countryId = country.id,
                    onSuccess = { cities ->
                        cityList.addAll(cities.mapModelListToBaseNodeList())
                        citiesCount += cities.size
                        country.childNode = cityList
                        /* since [setCities] function is launched inside a separate thread,
                        * [showVisitedCountryNodes] function must be in the same thread,
                        * otherwise result of this function will never get to the screen.
                        * */
                        if (country.id == visitedCountryWithCityNodes.last().id) {
                            // showing countries with included cities
                            showVisitedCountryNodes(
                                visitedCountryWithCityNodes,
                                visitedCountries
                            )
                        }
                    },
                    onError = { exception -> showError(exception) },
                )
            }
        }
    }

    private fun showVisitedCountryNodes(
        visitedCountryNodes: MutableList<VisitedCountryNode>,
        visitedCountries: List<VisitedCountry>
    ) {
        _visitedCountriesWithCitiesNode.run { postValue(visitedCountryNodes) }
        _visitedCountries.run { postValue(visitedCountries) }
        _visibilityLoader.postValue(GONE)
    }

    private fun showError(exception: Exception) {
        _visibilityLoader.postValue(GONE)
        _errorMessage.run {
            exception.message?.let { message ->
                // Trigger the event by setting a new Event as a new value
                postValue(Event(message))
            }
        }
    }
}