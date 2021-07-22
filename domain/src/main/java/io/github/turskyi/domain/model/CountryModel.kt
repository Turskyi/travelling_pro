package io.github.turskyi.domain.model

data class CountryModel(
    var id: Int,
    val name: String,
    val flag: String,
    @field:JvmField
    var isVisited: Boolean?,
    var selfie: String?,
    val selfieName: String,
) {
    // required empty constructor for firestore serialization
    constructor() : this(0, "", "", null, "", "")
    constructor(id: Int, name: String, flag: String) : this(
        id, name, flag, null, null, ""
    )

    constructor(id: Int, name: String, flag: String, isVisited: Boolean?) : this(
        id,
        name,
        flag,
        isVisited,
        null,
        ""
    )

    constructor(id: Int, name: String, flag: String, selfie: String?, selfieName: String) : this(
        id,
        name,
        flag,
        null,
        selfie,
        selfieName
    )
}
