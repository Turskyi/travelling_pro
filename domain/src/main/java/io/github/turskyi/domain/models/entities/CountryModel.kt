package io.github.turskyi.domain.models.entities

data class CountryModel(
    val id: Int,
    val name: String,
    val flag: String,
    @field:JvmField
    val isVisited: Boolean,
    val selfie: String,
    val selfieName: String,
) {
    // required empty constructor for firestore serialization
    constructor() : this(0, "", "", false, "", "")
    constructor(id: Int, name: String, flag: String) : this(
        id, name, flag, false, "", ""
    )

    constructor(id: Int, name: String, flag: String, isVisited: Boolean) : this(
        id,
        name,
        flag,
        isVisited,
        "",
        ""
    )

    constructor(id: Int, name: String, flag: String, selfie: String, selfieName: String) : this(
        id,
        name,
        flag,
        false,
        selfie,
        selfieName
    )
}
