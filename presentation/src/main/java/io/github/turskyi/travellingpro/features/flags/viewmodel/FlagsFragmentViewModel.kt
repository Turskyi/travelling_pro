package io.github.turskyi.travellingpro.features.flags.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedModelListToVisitedList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel(),
    LifecycleObserver {
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

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun getVisitedCountries() {
        viewModelScope.launch {
            interactor.setVisitedCountries({ countries ->
                visitedCount = countries.size
                _visitedCountries.run { postValue(countries.mapVisitedModelListToVisitedList()) }
                _visibilityLoader.postValue(GONE)
            }, { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    // Trigger the event by setting a new Event as a new value
                    postValue(
                        Event(
                            exception.localizedMessage ?: exception.stackTraceToString()
                        )
                    )
                }
            })
        }
    }

    fun updateSelfie(name: String, selfie: String, selfieName: String) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(IO) {
            interactor.updateSelfie(name, selfie, selfieName, { countries ->
                _visitedCountries.run { postValue(countries.mapVisitedModelListToVisitedList()) }
                _visibilityLoader.postValue(GONE)
            }, { exception ->
                _visibilityLoader.postValue(GONE)
                _errorMessage.run {
                    // Trigger the event by setting a new Event as a new value
                    postValue(
                        Event(
                            exception.localizedMessage ?: exception.stackTraceToString()
                        )
                    )
                }
            })
        }
    }
}