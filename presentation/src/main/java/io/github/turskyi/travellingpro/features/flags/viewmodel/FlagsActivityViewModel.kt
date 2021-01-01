package io.github.turskyi.travellingpro.features.flags.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import io.github.turskyi.domain.interactor.CountriesInteractor
import io.github.turskyi.travellingpro.extensions.mapModelListToActualList
import io.github.turskyi.travellingpro.models.Country

class FlagsActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {
    var visitedCount = 0

    private val _visitedCountries = MutableLiveData<List<Country>>()
    var visitedCountries: LiveData<List<Country>>

    init {
        visitedCountries = _visitedCountries
        getVisitedCountriesFromDB()
    }

    fun updateSelfie(name: String, selfie: String){
        viewModelScope.launch(IO) {
            interactor.updateSelfie(name,selfie, { countries ->
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
            }, {
                it.printStackTrace()
            })
        }
    }

    private fun getVisitedCountriesFromDB() {
        viewModelScope.launch {
            interactor.getVisitedModelCountries({ countries ->
                 visitedCount = countries.size
                _visitedCountries.run { postValue(countries.mapModelListToActualList()) }
            }, {
                it.printStackTrace()
            })
        }
    }
}