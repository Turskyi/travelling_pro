package io.github.turskyi.travellingpro.features.flags.view.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.koin.core.KoinComponent
import org.koin.core.inject
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.view.fragment.FlagFragment
import io.github.turskyi.travellingpro.features.flags.viewmodel.FlagsActivityViewModel

class ScreenSlidePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity), KoinComponent {
    private val viewModel: FlagsActivityViewModel by inject()
    override fun getItemCount(): Int = viewModel.visitedCount

    override fun createFragment(position: Int): Fragment = FlagFragment().apply {
        arguments = bundleOf(
            EXTRA_POSITION to position
        )
    }
}