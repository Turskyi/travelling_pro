package io.github.turskyi.data.repository

import io.github.turskyi.data.datasources.local.datastore.DataStoreDatabaseSource
import io.github.turskyi.domain.models.Authorization
import io.github.turskyi.domain.models.AuthorizationPreferences
import io.github.turskyi.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PreferenceRepositoryImpl : PreferenceRepository, KoinComponent {

    private val databaseSource: DataStoreDatabaseSource by inject()

    override val preferencesFlow: Flow<AuthorizationPreferences>
        get() = databaseSource.preferencesFlow

    override suspend fun updateAuthorization(authorization: Authorization) {
        databaseSource.updateAuthorization(authorization = authorization)
    }
}
