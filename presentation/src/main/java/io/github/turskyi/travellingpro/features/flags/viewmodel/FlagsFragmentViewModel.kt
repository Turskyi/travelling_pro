package io.github.turskyi.travellingpro.features.flags.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToCountryList
import io.github.turskyi.travellingpro.models.Country
import io.github.turskyi.travellingpro.utils.Event
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel(),
    LifecycleObserver {
    var visitedCount = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<Country>>()
    val visitedCountries: LiveData<List<Country>>
        get() = _visitedCountries

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun getVisitedCountries() {
        viewModelScope.launch {
            interactor.setVisitedModelCountries({ countries ->
                visitedCount = countries.size
                _visitedCountries.run { postValue(countries.mapModelListToCountryList()) }
                _visibilityLoader.postValue(GONE)
            }, {exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }

    fun updateSelfie(name: String, selfie: String, selfieName: String) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(IO) {
            interactor.updateSelfie(name, selfie, selfieName, { countries ->
                _visitedCountries.run { postValue(countries.mapModelListToCountryList()) }
                _visibilityLoader.postValue(GONE)
            }, {exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        /* Trigger the event by setting a new Event as a new value */
                        postValue(Event(message))
                    }
                }
            })
        }
    }
}