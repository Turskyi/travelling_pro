package io.github.turskyi.data.entities.local

data class TravellerEntity(
    val id: String,
    val name: String = "",
    val avatar: String,
    val counter: Long,
    // using [@field:JvmField] annotation if Boolean field is prefixed with 'is'
    @field:JvmField
    val isVisible: Boolean,
) {
    // required empty constructor for firestore serialization
    constructor() : this(id = "", name = "", avatar = "", counter = 0, isVisible = false)
    constructor(id: String) : this(id = id, name = "", avatar = "", counter = 0, isVisible = false)
    constructor(id: String, avatar: String) : this(
        id = id,
        name = "",
        avatar = avatar,
        counter = 0,
        isVisible = false,
    )
}