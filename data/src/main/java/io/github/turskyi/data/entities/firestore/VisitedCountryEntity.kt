package io.github.turskyi.data.entities.firestore

data class VisitedCountryEntity(
    val id: Int,
    val name: String,
    val flag: String,
    val selfie: String,
    val selfieName: String,
    val cities: MutableList<CityEntity>,
) {
    // required empty constructor for firestore serialization
    constructor() : this(
        id = 0,
        name = "",
        flag = "",
        selfie = "",
        selfieName = "",
        cities = mutableListOf()
    )

    constructor(name: String, flag: String) : this(
        id = 0,
        name = name,
        flag = flag,
        selfie = "",
        selfieName = "",
        cities = mutableListOf(),
    )

    constructor(id: Int, name: String, flag: String) : this(
        id,
        name,
        flag,
        "",
        "",
        mutableListOf(),
    )
}
