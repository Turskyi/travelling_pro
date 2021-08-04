package io.github.turskyi.travellingpro.features.flags.viewmodel

import android.view.View.GONE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.mapVisitedModelListToVisitedList
import kotlinx.coroutines.launch

class FriendFlagsFragmentViewModel(private val interactor: CountriesInteractor) : ViewModel() {
    var visitedCount: Int = 0

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    private val _visitedCountries: MutableLiveData<List<VisitedCountry>> =
        MutableLiveData<List<VisitedCountry>>()
    val visitedCountries: LiveData<List<VisitedCountry>>
        get() = _visitedCountries

    private val _errorMessage: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    fun setVisitedCountries(userId: String) {
        viewModelScope.launch {
            interactor.setVisitedCountries(
                id = userId,
                onSuccess = { countries ->
                    visitedCount = countries.size
                    _visitedCountries.run { postValue(countries.mapVisitedModelListToVisitedList()) }
                    _visibilityLoader.postValue(GONE)
                },
                onError = { exception ->
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