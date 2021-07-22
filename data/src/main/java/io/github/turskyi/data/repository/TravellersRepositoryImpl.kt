package io.github.turskyi.data.repository

import io.github.turskyi.data.extensions.mapFirestoreListToModelList
import io.github.turskyi.data.firestore.service.FirestoreSource
import io.github.turskyi.domain.model.TravellerModel
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
}