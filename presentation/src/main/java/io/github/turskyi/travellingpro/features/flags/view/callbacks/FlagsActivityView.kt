package io.github.turskyi.travellingpro.features.flags.view.callbacks

interface FlagsActivityView {
    fun getItemCount(): Int
    fun setLoaderVisibility(currentVisibility: Int)
}