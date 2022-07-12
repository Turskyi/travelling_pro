package io.github.turskyi.data.datasources.local.datastore

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import io.github.turskyi.domain.models.Authorization
import io.github.turskyi.domain.models.AuthorizationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import java.io.IOException

class DataStoreDatabaseSourceImpl(application: Application) : KoinComponent,
    DataStoreDatabaseSource {

    companion object {
        // constants for datastore
        private const val KEY_AUTHORIZATION = "authorization"
    }

    private val Context.dataStore by preferencesDataStore("user_preferences")
    private val dataStore: DataStore<Preferences> = application.dataStore

    override val preferencesFlow: Flow<AuthorizationPreferences> = dataStore.data
        .catch { exception: Throwable ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences: Preferences ->
            val sortOrder: Authorization = Authorization.valueOf(
                preferences[PreferencesKeys.AUTHORIZATION] ?: Authorization.IS_SIGNED_OUT.name
            )
            AuthorizationPreferences(sortOrder)
        }

    override suspend fun updateAuthorization(authorization: Authorization) {
        dataStore.edit { preferences: MutablePreferences ->
            preferences[PreferencesKeys.AUTHORIZATION] = authorization.name
        }
    }

    private object PreferencesKeys {
        val AUTHORIZATION = stringPreferencesKey(KEY_AUTHORIZATION)
    }
}