package io.github.turskyi.domain.repository

import io.github.turskyi.domain.models.Authorization
import io.github.turskyi.domain.models.AuthorizationPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val preferencesFlow: Flow<AuthorizationPreferences>
    suspend fun updateAuthorization(authorization: Authorization)
}