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
    constructor(name: String, flag: String) : this(
        id = 0, name = name, flag = flag, isVisited = false, selfie = "", selfieName = ""
    )
}
