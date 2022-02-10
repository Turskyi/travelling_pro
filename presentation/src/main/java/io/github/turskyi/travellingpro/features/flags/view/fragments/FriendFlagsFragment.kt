package io.github.turskyi.travellingpro.features.flags.view.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_USER
import io.github.turskyi.travellingpro.features.flags.viewmodel.FriendFlagsFragmentViewModel
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import org.koin.android.ext.android.inject

class FriendFlagsFragment : BaseFlagFragment() {

    private val viewModel: FriendFlagsFragmentViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (this.arguments != null && this.requireArguments()
                .getParcelable<Traveller>(EXTRA_USER) != null
        ) {
            val traveller: Traveller = this.requireArguments().getParcelable(EXTRA_USER)!!
            viewModel.setVisitedCountries(traveller.id)
        } else {
            toast(R.string.msg_not_found)
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        initObservers()
    }

    private fun initObservers() {
        viewModel.errorMessage.observe(this) { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility: Int ->
            flagsActivityViewListener?.setLoaderVisibility(currentVisibility)
        }
        val visitedCountriesObserver: Observer<List<VisitedCountry>> =
            Observer<List<VisitedCountry>> { countries ->
                val position: Int = this.requireArguments().getInt(EXTRA_POSITION)
                if (mChangeFlagListener != null) {
                    mChangeFlagListener!!.onChangeToolbarTitle(countries[position].title)
                    if (countries[position].selfie.isEmpty()) {
                        showTheFlag(countries, position)
                    } else {
                        showSelfie(countries, position)
                        binding.ivEnlargedFlag.setOnClickListener(
                            showFlagClickListener(countries, position)
                        )
                    }
                } else {
                    toast(R.string.msg_not_found)
                    requireActivity().finish()
                }
            }
        viewModel.visitedCountries.observe(viewLifecycleOwner, visitedCountriesObserver)
    }
}