package io.github.turskyi.data.entities.local

data class VisitedCountryEntity(
    val id: Int,
    val name: String,
    val flag: String,
    val selfie: String,
    val selfieName: String,
) {
    // required empty constructor for firestore serialization
    constructor() : this(
        id = 0,
        name = "",
        flag = "",
        selfie = "",
        selfieName = "",
    )

    constructor(id: Int, name: String, flag: String) : this(
        id,
        name,
        flag,
        "",
        "",
    )
}
