package io.github.turskyi.data.repository

import io.github.turskyi.data.util.extensions.mapFirestoreListToModelList
import io.github.turskyi.data.firestore.service.FirestoreSource
import io.github.turskyi.domain.entities.TravellerModel
import io.github.turskyi.domain.repository.TravellersRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravellersRepositoryImpl : TravellersRepository, KoinComponent {
    private val firestoreSource: FirestoreSource by inject()
    override fun setTravellersByName(
        name: String,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestoreSource.setTravellersByName(
            name,
            { list -> onSuccess(list.mapFirestoreListToModelList()) },
            { onError.invoke(it) },
        )
    }

    override fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        firestoreSource.setTopTravellersPercent(
            { percent -> onSuccess(percent) },
            { exception -> onError.invoke(exception) },
        )
    }
}