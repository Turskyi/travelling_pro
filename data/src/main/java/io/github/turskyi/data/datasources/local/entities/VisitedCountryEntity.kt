@file:Suppress("unused")

package io.github.turskyi.data.datasources.local.entities

data class VisitedCountryEntity(
    val id: Int,
    val shortName: String,
    val name: String,
    val flag: String,
    val selfie: String,
    val selfieName: String,
) {
    // required empty constructor for firestore serialization
    constructor() : this(
        id = 0,
        shortName = "",
        name = "",
        flag = "",
        selfie = "",
        selfieName = "",
    )

    constructor(id: Int, shortName: String, name: String, flag: String) : this(
        id,
        shortName,
        name,
        flag,
        "",
        "",
    )
}
