package io.github.turskyi.travellingpro.common.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.turskyi.data.BuildConfig
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
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.domain.interactors.PreferenceInteractor
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.domain.repository.CountryRepository
import io.github.turskyi.domain.repository.PreferenceRepository
import io.github.turskyi.domain.repository.TravellerRepository
import io.github.turskyi.travellingpro.features.countries.view.adapter.AllCountriesAdapter
import io.github.turskyi.travellingpro.features.countries.viewmodel.AllCountriesActivityViewModel
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsFragmentViewModel
import io.github.turskyi.travellingpro.features.flags.viewmodel.FriendFlagsFragmentViewModel
import io.github.turskyi.travellingpro.features.home.view.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.AddCityDialogViewModel
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel
import io.github.turskyi.travellingpro.features.traveller.TravellerActivityViewModel
import io.github.turskyi.travellingpro.features.traveller.view.VisitedCountriesAdapter
import io.github.turskyi.travellingpro.features.travellers.TravellersActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersAdapter
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

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { VisitedCountriesAdapter() }
    factory { AllCountriesAdapter() }
    factory { TravellersAdapter() }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get(), get()) }
    factory { TravellerActivityViewModel(get()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsFragmentViewModel(get()) }
    factory { FriendFlagsFragmentViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
    factory { TravellersActivityViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor(CoroutineScope(SupervisorJob())) }
    factory { TravellersInteractor() }
    factory { PreferenceInteractor() }
}

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
            .baseUrl(BuildConfig.HOST_URL)
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

