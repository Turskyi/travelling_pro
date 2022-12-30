package io.github.turskyi.data.repository

import io.github.turskyi.data.datasources.local.firestore.FirestoreDatabaseSource
import io.github.turskyi.data.util.extensions.mapFirestoreListToModelList
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.repository.TravellerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravellerRepositoryImpl(private val applicationScope: CoroutineScope) : TravellerRepository,
    KoinComponent {
    private val databaseSource: FirestoreDatabaseSource by inject()
    override fun setTravellersByName(
        name: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            databaseSource.setTravellersByName(
                name,
                requestedLoadSize,
                requestedStartPosition,
                { list -> onSuccess(list.mapFirestoreListToModelList()) },
                { onError.invoke(it) },
            )
        }
    }

    override fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        applicationScope.launch {
            databaseSource.setTopTravellersPercent(
                onSuccess = { percent: Int -> onSuccess(percent) },
                onError = { exception: Exception -> onError.invoke(exception) },
            )
        }
    }

    override fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        applicationScope.launch { databaseSource.saveTraveller(onSuccess, onError) }
    }

    override fun setTravellersByRange(
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch {
            databaseSource.setTravellersByRange(
                to = requestedLoadSize,
                from = requestedStartPosition,
                onSuccess = { list -> onSuccess(list.mapFirestoreListToModelList()) },
                onError = { onError.invoke(it) },
            )
        }
    }

    override fun setUserVisibility(
        visible: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch { databaseSource.setUserVisibility(visible, onSuccess, onError) }
    }

    override fun setUserVisibility(onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        applicationScope.launch { databaseSource.setUserVisibility(onSuccess, onError) }
    }
}