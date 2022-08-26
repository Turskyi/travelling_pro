package io.github.turskyi.travellingpro.features.flags.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedModelListToVisitedList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel(),
    LifecycleEventObserver {
    private var visitedCount = 0

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries = MutableLiveData<List<VisitedCountry>>()
    val visitedCountries: LiveData<List<VisitedCountry>>
        get() = _visitedCountries

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            getVisitedCountries()
        }
    }

    fun updateSelfie(shortName: String, filePath: String, selfieName: String) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(IO) {
            interactor.updateSelfie(
                shortName = shortName,
                filePath = filePath,
                selfieName = selfieName,
                onSuccess = { countries: List<VisitedCountryModel> ->
                    _visitedCountries.postValue(countries.mapVisitedModelListToVisitedList())
                    _visibilityLoader.postValue(GONE)
                },
                onError = { exception: Exception /* = java.lang.Exception */ ->
                    _visibilityLoader.postValue(GONE)
                    _errorMessage.run {
                        // Trigger the event by setting a new Event as a new value
                        postValue(
                            Event(
                                exception.localizedMessage ?: exception.stackTraceToString()
                            )
                        )
                    }
                },
            )
        }
    }

    private fun getVisitedCountries() {
        viewModelScope.launch {
            interactor.setVisitedCountries(
                onSuccess = { countries: List<VisitedCountryModel> ->
                    visitedCount = countries.size
                    _visitedCountries.postValue(countries.mapVisitedModelListToVisitedList())
                    _visibilityLoader.postValue(GONE)
                },
                onError = { exception: Exception /* = java.lang.Exception */ ->
                    _visibilityLoader.postValue(GONE)
                    _errorMessage.run {
                        // Trigger the event by setting a new Event as a new value
                        postValue(
                            Event(
                                exception.localizedMessage ?: exception.stackTraceToString()
                            )
                        )
                    }
                },
            )
        }
    }
}