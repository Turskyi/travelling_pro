package io.github.turskyi.data.entities.local

data class CityEntity(
    var id: String,
    val name: String,
    val parentId: Int,
    val month: String
) {
    // required empty constructor for firestore serialization
    constructor() : this("", "", 0, "")
}
