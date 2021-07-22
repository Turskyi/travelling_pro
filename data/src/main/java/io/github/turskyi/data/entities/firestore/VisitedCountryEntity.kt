package io.github.turskyi.data.entities.firestore

data class VisitedCountryEntity(
    val id: Int,
    val name: String,
    val flag: String,
    val selfie: String?,
    val selfieName: String,
    val cities: List<CityEntity>,
) {
    // required empty constructor for firestore serialization
    constructor() : this(
        id = 0,
        name = "",
        flag = "",
        selfie = null,
        selfieName = "",
        cities = emptyList()
    )

    constructor(name: String, flag: String) : this(0, name, flag, null, "", emptyList())
    constructor(id: Int, name: String, flag: String) : this(id, name, flag, null, "", emptyList())
}
