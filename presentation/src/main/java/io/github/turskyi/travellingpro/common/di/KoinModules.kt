package io.github.turskyi.travellingpro.common.di

import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.domain.interactors.PreferenceInteractor
import io.github.turskyi.domain.interactors.TravellersInteractor
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
import org.koin.dsl.module

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

