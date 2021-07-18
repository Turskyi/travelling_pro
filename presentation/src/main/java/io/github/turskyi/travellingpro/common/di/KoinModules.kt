package io.github.turskyi.travellingpro.common.di

import io.github.turskyi.domain.interactor.CountriesInteractor
import io.github.turskyi.travellingpro.features.allcountries.view.adapter.AllCountriesAdapter
import io.github.turskyi.travellingpro.features.allcountries.viewmodel.AllCountriesActivityViewModel
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsFragmentViewModel
import io.github.turskyi.travellingpro.features.home.view.adapter.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.AddCityDialogViewModel
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel
import org.koin.dsl.module

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsFragmentViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
}

