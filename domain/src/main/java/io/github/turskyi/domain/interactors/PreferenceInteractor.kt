package io.github.turskyi.domain.interactors

import io.github.turskyi.domain.models.Authorization
import io.github.turskyi.domain.models.AuthorizationPreferences
import io.github.turskyi.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PreferenceInteractor : KoinComponent {
    private val preferenceRepository: PreferenceRepository by inject()

    val preferencesFlow: Flow<AuthorizationPreferences>
        get() = preferenceRepository.preferencesFlow

    suspend fun updateAuthorization(authorization: Authorization) {
        preferenceRepository.updateAuthorization(authorization = authorization)
    }
}