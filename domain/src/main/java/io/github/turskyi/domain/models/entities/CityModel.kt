package io.github.turskyi.domain.models.entities

data class CityModel(
    val id: String,
    var name: String,
    var parentId: Int,
    var month: String
) {
    // required empty constructor for firestore serialization
    constructor() : this("", "", 0, "")
}
