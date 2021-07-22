package io.github.turskyi.domain.model

data class CityModel(
    var name: String,
    var parentId: Int,
    var month: String
) {
    // required empty constructor for firestore serialization
    constructor() : this( "", 0, "")
}
