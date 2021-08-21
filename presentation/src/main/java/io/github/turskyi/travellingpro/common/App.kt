@file:Suppress("unused")

package io.github.turskyi.travellingpro.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.appevents.AppEventsLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import io.github.turskyi.di.dataProvidersModule
import io.github.turskyi.di.repositoriesModule
import io.github.turskyi.di.sourcesModule
import io.github.turskyi.travellingpro.common.di.adaptersModule
import io.github.turskyi.travellingpro.common.di.interactorsModule
import io.github.turskyi.travellingpro.common.di.viewModelsModule

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

        /** init facebook sdk */
        AppEventsLogger.activateApp(this)
    }
}