package io.github.turskyi.travellingpro.features.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.utils.extensions.mapNodeToModel
import io.github.turskyi.travellingpro.models.City

class AddCityDialogViewModel(private val interactor: CountriesInteractor) : ViewModel() {
    fun insert(
        city: City,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) {
        viewModelScope.launch {
            interactor.insertCity(city.mapNodeToModel(), onSuccess = onSuccess, onError = onError)
        }
    }
}