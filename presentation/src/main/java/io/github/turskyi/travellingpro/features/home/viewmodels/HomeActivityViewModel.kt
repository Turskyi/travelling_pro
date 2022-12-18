package io.github.turskyi.travellingpro.features.home.viewmodels

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.domain.interactors.PreferenceInteractor
import io.github.turskyi.domain.models.Authorization
import io.github.turskyi.domain.models.AuthorizationPreferences
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import io.github.turskyi.travellingpro.features.home.view.ui.HomeActivity
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeActivityViewModel(
    private val countriesInteractor: CountriesInteractor,
    private val preferenceInteractor: PreferenceInteractor,
) : ViewModel() {

    var isDoubleBackToExitPressed = false
    var notVisitedCountriesCount: Float = 0F
    var cityCount: Int = 0
    var mLastClickTime: Long = 0
    var isPermissionGranted: Boolean = false

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

    private val _navigateToAllCountries: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val navigateToAllCountries: LiveData<Boolean>
        get() = _navigateToAllCountries

    val preferencesFlow: Flow<AuthorizationPreferences> = preferenceInteractor.preferencesFlow

    /** [showListOfVisitedCountries] is the first function in [HomeActivity],
     *  which is triggered when permission and authorization is completed */
    fun showListOfVisitedCountries() {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            // loading count of not visited countries
            countriesInteractor.setNotVisitedCountriesNum(
                onSuccess = { notVisitedCountriesNum: Int ->
                    // loading visited countries
                    setVisitedCountries(notVisitedCountriesNum)
                },
                onError = { exception: Exception /* = java.lang.Exception */ ->
                    showError(exception)
                },
            )
        }
    }

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    fun removeFromVisited(country: Country) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        countriesInteractor.removeCountryModelFromVisitedList(
            country.mapToModel(),
            { showListOfVisitedCountries() },
            { exception -> showError(exception) },
        )
    }

    fun removeCity(city: City) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        countriesInteractor.removeCity(
            city.mapNodeToModel(),
            { showListOfVisitedCountries() },
            { exception -> showError(exception) },
        )
    }

    fun onAuthorizationSignedId(authorization: Authorization) = viewModelScope.launch {
        preferenceInteractor.updateAuthorization(authorization)
    }

    private fun setVisitedCountries(notVisitedCountriesNum: Int) {
        viewModelScope.launch {
            countriesInteractor.setVisitedCountries(
                { countries ->
                    val visitedCountries: List<VisitedCountry> =
                        countries.mapVisitedModelListToVisitedList()
                    // checking if database of visited and not visited countries is empty
                    if (notVisitedCountriesNum == 0 && visitedCountries.isEmpty()) {
                        /* if both lists are empty it means, that countries are not downloaded
                         * to local database, so we have to download them first */
                        viewModelScope.launch { downloadCountries() }
                    } else {
                        notVisitedCountriesCount = notVisitedCountriesNum.toFloat()
                        val visitedCountryWithCityNodes: MutableList<VisitedCountryNode> =
                            visitedCountries.mapVisitedListToVisitedNodeList()
                        if (visitedCountryWithCityNodes.isEmpty()) {
                            // if there are no countries there are no cities, show empty list
                            showVisitedCountryNodes(mutableListOf(), emptyList())
                        } else {
                            fillCountriesWithCities(visitedCountryWithCityNodes, visitedCountries)
                            /* do not write any logic after  countries loop (here),
                             * rest of the logic must be in "get cities" success method ,
                             * since it started later then here */
                        }
                    }
                },
                { exception -> showError(exception) },
            )
        }
    }

    /** filling country nodes with cities */
    private fun fillCountriesWithCities(
        visitedCountryWithCityNodes: MutableList<VisitedCountryNode>,
        visitedCountries: List<VisitedCountry>
    ) {
        for (country in visitedCountryWithCityNodes) {
            val cityList: MutableList<BaseNode> = mutableListOf()
            viewModelScope.launch {
                countriesInteractor.setCities(
                    parentId = country.id,
                    onSuccess = { cities ->
                        cityList.addAll(cities.mapModelListToBaseNodeList())
                        country.childNode = cityList
                        /* since [setCitiesById] function is launched inside a separate thread,
                         * [showVisitedCountryNodes] function must be in the same thread,
                         * otherwise result of this function will never get to the screen */
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
        _visitedCountries.postValue(visitedCountries)
        _visibilityLoader.postValue(GONE)
    }

    private suspend fun downloadCountries() {
        countriesInteractor.downloadCountries(
            { showListOfVisitedCountries() },
            { exception -> showError(exception) },
        )
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