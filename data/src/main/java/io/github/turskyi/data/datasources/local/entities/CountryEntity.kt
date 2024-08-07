@file:Suppress("unused")

package io.github.turskyi.data.datasources.local.entities

/** Object to use for storing all countries in firestore database */
data class CountryEntity(
        val id: Int,
        val shortName: String,
        val name: String,
        val flag: String,
        /** parameter to use on the "all countries" page, to show if the country was already visited*/
        @field:JvmField
        val isVisited: Boolean,
) {
        // required empty constructor for firestore serialization
        constructor() : this(0, "", "", "", false)
}
