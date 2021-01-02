package io.github.turskyi.travellingpro.features.flags.callbacks

interface FlagsActivityView {
    fun getItemCount(): Int
    fun setLoaderVisibility(currentVisibility: Int)
}