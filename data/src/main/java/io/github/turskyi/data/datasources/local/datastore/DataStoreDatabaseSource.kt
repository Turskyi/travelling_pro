package io.github.turskyi.data.datasources.local.datastore

import io.github.turskyi.domain.models.Authorization
import io.github.turskyi.domain.models.AuthorizationPreferences
import kotlinx.coroutines.flow.Flow

interface DataStoreDatabaseSource {
    val preferencesFlow: Flow<AuthorizationPreferences>
    suspend fun updateAuthorization(authorization: Authorization)
}
