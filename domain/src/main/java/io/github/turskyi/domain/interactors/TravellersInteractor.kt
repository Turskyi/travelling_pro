package io.github.turskyi.domain.interactors

import io.github.turskyi.domain.model.TravellerModel
import io.github.turskyi.domain.repository.TravellersRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravellersInteractor : KoinComponent {
    private val repository: TravellersRepository by inject()

    fun setTravellersByName(
        name: String,
        onSusses: (List<TravellerModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ): Unit = repository.setTravellersByName(name, onSusses, onError)
}