package io.github.turskyi.data.entities.firestore

data class CountryEntity(
        var id: Int,
        val name: String,
        val flag: String,
        @field:JvmField
        var isVisited: Boolean?,
){
        // required empty constructor for firestore serialization
        constructor() : this(0, "", "", null)
}
