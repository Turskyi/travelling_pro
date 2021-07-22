package io.github.turskyi.di
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.github.turskyi.data.BuildConfig.HOST_URL
import io.github.turskyi.data.network.datasource.CountriesNetSource
import io.github.turskyi.data.network.service.CountriesApi
import io.github.turskyi.data.firestore.dataSource.FirestoreSourceImpl
import io.github.turskyi.data.firestore.service.FirestoreSource
import io.github.turskyi.data.util.hasNetwork
import io.github.turskyi.data.repository.CountriesRepositoryImpl
import io.github.turskyi.data.repository.TravellersRepositoryImpl
import io.github.turskyi.domain.repository.CountriesRepository
import io.github.turskyi.domain.repository.TravellersRepository
import okhttp3.Request

val repositoriesModule = module {
    factory<CountriesRepository> { CountriesRepositoryImpl() }
    factory<TravellersRepository> { TravellersRepositoryImpl() }
}

val dataProvidersModule = module {
    single {
        OkHttpClient.Builder()
            .cache(get<Cache>())
            .addInterceptor { chain ->
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
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>())).build()
    }
}

val sourcesModule = module {
    single { get<Retrofit>().create(CountriesApi::class.java) }
    single { CountriesNetSource(get()) }
    factory<FirestoreSource> { FirestoreSourceImpl() }
}