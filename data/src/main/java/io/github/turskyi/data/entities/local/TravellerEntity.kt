package io.github.turskyi.data.entities.local

data class TravellerEntity(
    val id: String,
    val name: String = "",
    val avatar: String,
    @field:JvmField
    val isVisible: Boolean,
) {
    // required empty constructor for firestore serialization
    constructor() : this(id = "", name = "", avatar = "", isVisible = false)
    constructor(id: String) : this(id = id, name = "", avatar = "", isVisible = false)
    constructor(id: String, avatar: String) : this(
        id = id,
        name = "",
        avatar = avatar,
        isVisible = false,
    )
}