@file:Suppress("unused", "RedundantSuppression")

package io.github.turskyi.travellingpro.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import io.github.turskyi.travellingpro.common.di.adaptersModule
import io.github.turskyi.travellingpro.common.di.dataProvidersModule
import io.github.turskyi.travellingpro.common.di.interactorsModule
import io.github.turskyi.travellingpro.common.di.repositoriesModule
import io.github.turskyi.travellingpro.common.di.sourcesModule
import io.github.turskyi.travellingpro.common.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate()

        startKoin {
            androidContext(applicationContext)
            modules(
                listOf(
                    sourcesModule,
                    dataProvidersModule,
                    adaptersModule,
                    viewModelsModule,
                    interactorsModule,
                    repositoriesModule,
                )
            )
        }
    }
}