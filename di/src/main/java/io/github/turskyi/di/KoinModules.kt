package io.github.turskyi.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.turskyi.data.BuildConfig.HOST_URL
import io.github.turskyi.data.datasources.local.datastore.DataStoreDatabaseSource
import io.github.turskyi.data.datasources.local.datastore.DataStoreDatabaseSourceImpl
import io.github.turskyi.data.datasources.local.firestore.FirestoreDatabaseSource
import io.github.turskyi.data.datasources.local.firestore.FirestoreDatabaseSourceImpl
import io.github.turskyi.data.datasources.remote.CountriesApi
import io.github.turskyi.data.datasources.remote.NetSource
import io.github.turskyi.data.repository.CountryRepositoryImpl
import io.github.turskyi.data.repository.PreferenceRepositoryImpl
import io.github.turskyi.data.repository.TravellerRepositoryImpl
import io.github.turskyi.data.util.hasNetwork
import io.github.turskyi.domain.repository.CountryRepository
import io.github.turskyi.domain.repository.PreferenceRepository
import io.github.turskyi.domain.repository.TravellerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val repositoriesModule: Module = module {
    factory<CountryRepository> { CountryRepositoryImpl(CoroutineScope(SupervisorJob())) }
    factory<TravellerRepository> { TravellerRepositoryImpl(CoroutineScope(SupervisorJob())) }
    factory<PreferenceRepository> { PreferenceRepositoryImpl() }
}

val dataProvidersModule: Module = module {
    single {
        OkHttpClient.Builder()
            .cache(get())
            .addInterceptor { chain: Interceptor.Chain ->
                var request: Request = chain.request()
                request = if (hasNetwork(androidContext())) {
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, max-age=" + 5
                    ).build()
                } else {
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + (60 * 60 * 24 * 7)
                    ).build()
                }
                chain.proceed(request)
            }.addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    single<Gson> { GsonBuilder().setLenient().create() }

    single {
        val cacheSize: Long = (5 * 1024 * 1024).toLong()
        Cache(androidContext().cacheDir, cacheSize)
    }
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(HOST_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get())).build()
    }
}

val sourcesModule: Module = module {
    single { get<Retrofit>().create(CountriesApi::class.java) }
    single { NetSource(get()) }
    factory<FirestoreDatabaseSource> {
        FirestoreDatabaseSourceImpl(androidApplication(), CoroutineScope(SupervisorJob()))
    }
    factory<DataStoreDatabaseSource> { DataStoreDatabaseSourceImpl(androidApplication()) }
}