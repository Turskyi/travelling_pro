package io.github.turskyi.travellingpro.common.di

import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.features.allcountries.view.adapter.AllCountriesAdapter
import io.github.turskyi.travellingpro.features.allcountries.viewmodel.AllCountriesActivityViewModel
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsFragmentViewModel
import io.github.turskyi.travellingpro.features.home.view.adapter.HomeAdapter
import io.github.turskyi.travellingpro.features.home.viewmodels.AddCityDialogViewModel
import io.github.turskyi.travellingpro.features.home.viewmodels.HomeActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.TravellersActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersAdapter
import org.koin.dsl.module

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
    factory { TravellersAdapter() }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsFragmentViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
    factory { TravellersActivityViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
    factory { TravellersInteractor() }
}

