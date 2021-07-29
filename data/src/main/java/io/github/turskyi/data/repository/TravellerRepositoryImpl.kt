package io.github.turskyi.data.repository

import io.github.turskyi.data.util.extensions.mapFirestoreListToModelList
import io.github.turskyi.data.database.firestore.service.FirestoreDatabaseSource
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.repository.TravellerRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravellerRepositoryImpl : TravellerRepository, KoinComponent {
    private val databaseSource: FirestoreDatabaseSource by inject()
    override fun setTravellersByName(
        name: String,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        databaseSource.setTravellersByName(
            name,
            { list -> onSuccess(list.mapFirestoreListToModelList()) },
            { onError.invoke(it) },
        )
    }

    override fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        databaseSource.setTopTravellersPercent(
            { percent -> onSuccess(percent) },
            { exception -> onError.invoke(exception) },
        )
    }

    override fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        databaseSource.saveTraveller(onSuccess, onError)
    }

    override fun setTravellersByRange(
        requestedLoadSize: Int,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        databaseSource.setTravellersByRange(
            to = requestedLoadSize,
            from = requestedStartPosition,
            onSuccess = { list -> onSuccess(list.mapFirestoreListToModelList()) },
            onError = { onError.invoke(it) },
        )
    }
}