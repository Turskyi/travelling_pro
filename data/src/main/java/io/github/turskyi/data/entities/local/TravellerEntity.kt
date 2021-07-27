package io.github.turskyi.data.entities.local

data class TravellerEntity(val id: String, val name: String, val avatar: String) {
    // required empty constructor for firestore serialization
    constructor() : this("", "", "")
}