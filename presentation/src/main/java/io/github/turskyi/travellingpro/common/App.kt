package io.github.turskyi.travellingpro.common

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
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

        /* allows using cached data while offline */
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            /**
             * The Firebase Database client will cache synchronized data and keep track of all writes you've
             * initiated while your application is running. It seamlessly handles intermittent network
             * connections and re-sends write operations when the network connection is restored.
             *
             * <p>However by default your write operations and cached data are only stored in-memory and will
             * be lost when your app restarts. By setting this value to `true`, the data will be persisted to
             * on-device (disk) storage and will thus be available again when the app is restarted (even when
             * there is no network connectivity at that time). Note that this method must be called before
             * creating your first Database reference and only needs to be called once per application.
             *
             * @param .setPersistenceEnabled set to true to enable disk persistence, set to false to disable it.
             */
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }

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