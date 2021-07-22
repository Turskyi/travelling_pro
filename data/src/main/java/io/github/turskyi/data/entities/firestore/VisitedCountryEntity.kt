package io.github.turskyi.data.entities.firestore

data class VisitedCountryEntity(
    var id: Int,
    val name: String,
    val flag: String,
    val cities: List<CityEntity>,
    val selfie: String?,
    val selfieName: String?,
) {
    // required empty constructor for firestore serialization
    constructor() : this(0, "", "", emptyList(), null, null)
    constructor(name: String, flag: String) : this(0, name, flag, emptyList(), null, null)
    constructor(id: Int, name: String, flag: String) : this(id, name, flag, emptyList(), null, null)
}
