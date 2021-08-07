package io.github.turskyi.domain.models.entities

data class TravellerModel(
    val id: String,
    val name: String,
    val avatar: String,
    val counter: Int,
    val isVisible: Boolean,
) {
    // required empty constructor for firestore serialization
    constructor() : this(id = "", name = "", avatar = "", isVisible = false, counter = 0)
}