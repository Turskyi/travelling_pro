package io.github.turskyi.data.entities.firestore

data class CountryEntity(
        var id: Int,
        val name: String,
        val flag: String,
        @field:JvmField
        var isVisited: Boolean?,
        var selfie: String?,
){
        /* required empty constructor for firestore serialization */
        constructor() : this(0, "", "", null, "")
        constructor(id: Int, name: String, flag: String, isVisited: Boolean?) : this(id, name, flag, isVisited, null)
}
