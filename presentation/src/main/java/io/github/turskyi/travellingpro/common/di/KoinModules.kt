package io.github.turskyi.travellingpro.common.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import io.github.turskyi.domain.interactor.CountriesInteractor
import io.github.turskyi.travellingpro.features.allcountries.view.adapter.AllCountriesAdapter
import io.github.turskyi.travellingpro.features.allcountries.viewmodel.AllCountriesActivityViewModel
import io.github.turskyi.travellingpro.features.flags.view.adapter.ScreenSlidePagerAdapter
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsActivityViewModel
import io.github.turskyi.travellingpro.features.home.view.adapter.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.AddCityDialogViewModel
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
    factory { ScreenSlidePagerAdapter(get()) }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get(), androidApplication()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsActivityViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
}

