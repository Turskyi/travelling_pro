package io.github.turskyi.travellingpro.features.home.viewmodels

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.entity.node.BaseNode
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.*
import kotlinx.coroutines.launch

class HomeActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    var backPressedTiming: Long = 0
    var notVisitedCountriesCount: Float = 0F
    var citiesCount: Int = 0
    var mLastClickTime: Long = 0
    var isPermissionGranted: Boolean = false

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries: MutableLiveData<List<VisitedCountry>> = MutableLiveData<List<VisitedCountry>>()
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

    fun showListOfVisitedCountries() {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch {

            // loading count of not visited countries
            interactor.setNotVisitedCountriesNum({ notVisitedCountriesNum ->
                notVisitedCountriesCount = notVisitedCountriesNum.toFloat()

                // loading visited countries
                setVisitedCountries(notVisitedCountriesNum)
            }, { exception ->
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Trigger the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    private fun setVisitedCountries(notVisitedCountriesNum: Int) {
        viewModelScope.launch {
            interactor.setVisitedModelCountries({ visitedCountries ->
              val countries: List<VisitedCountry> = visitedCountries.mapVisitedModelListToVisitedList()
                // checking if database of visited and not visited countries is empty
                if (notVisitedCountriesNum == 0 && countries.isNullOrEmpty()) {
                    viewModelScope.launch { downloadCountries() }
                } else {
                    addCitiesToVisitedCountriesIfNotEmpty(countries)
                }
            }, { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Trigger the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    private fun addCitiesToVisitedCountriesIfNotEmpty(countries: List<VisitedCountry>) {
        val visitedCountryNodes: MutableList<VisitedCountryNode> = countries.mapVisitedListToVisitedNodeList()
        if (visitedCountryNodes.isEmpty()) {
            _visitedCountriesWithCitiesNode.run { postValue(visitedCountryNodes) }
            _visitedCountries.run { postValue(countries) }
            _visibilityLoader.postValue(GONE)
        } else {
            for (country in visitedCountryNodes) {
                val cityList = mutableListOf<BaseNode>()
                viewModelScope.launch {
                    interactor.setCities({ cities ->
                        for (city in cities) {
                            if (country.id == city.parentId) {
                                cityList.add(city.mapModelToBaseNode())
                            }
                        }
                        citiesCount = cities.size
                        country.childNode = cityList
                        if (country.id == visitedCountryNodes.last().id) {
                            // showing countries with included cities
                            _visitedCountriesWithCitiesNode.run { postValue(visitedCountryNodes) }
                            _visitedCountries.run { postValue(countries) }
                            _visibilityLoader.postValue(GONE)
                        }
                    }, { exception ->
                        _visibilityLoader.postValue(GONE)
                        _errorMessage.run {
                            exception.message?.let { message ->
                                // Trigger the event by setting a new Event as a new value
                                postValue(Event(message))
                            }
                        }
                    })
                }
            }
            /* do not write any logic after  countries loop (here),
             * rest of the logic must be in "get cities" success method ,
             * since it started later then here */
        }
    }

    private suspend fun downloadCountries() = interactor.downloadCountries({
        showListOfVisitedCountries()
    }, { exception ->
        _visibilityLoader.postValue(GONE)
        _errorMessage.run {
            exception.message?.let { message ->
                // Trigger the event by setting a new Event as a new value
                postValue(Event(message))
            }
        }
    })

    fun onFloatBtnClicked() {
        _navigateToAllCountries.value = true
    }

    fun onNavigatedToAllCountries() {
        _navigateToAllCountries.value = false
    }

    fun removeFromVisited(country: Country) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        interactor.removeCountryModelFromVisitedList(country.mapToModel(), {
            showListOfVisitedCountries()
        }, { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.run {
                exception.message?.let { message ->
                    // Trigger the event by setting a new Event as a new value
                    postValue(Event(message))
                }
            }
        })
    }

    fun removeCity(city: City) = viewModelScope.launch {
        _visibilityLoader.postValue(VISIBLE)
        interactor.removeCity(city.mapNodeToModel(), {
            showListOfVisitedCountries()
        }, { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.run {
                exception.message?.let { message ->
                    // Trigger the event by setting a new Event as a new value
                    postValue(Event(message))
                }
            }
        })
    }
}