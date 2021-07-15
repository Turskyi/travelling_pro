package io.github.turskyi.data.entities.firestore

data class VisitedCountryEntity(
    var id: Int,
    val name: String,
    val flag: String,
    var selfie: String?,
    var selfieName: String?,
) {
    // required empty constructor for firestore serialization
    constructor() : this(0, "", "", null, null)
    constructor(name: String, flag: String) : this(0, name, flag, null, null)
    constructor(id: Int, name: String, flag: String) : this(id, name, flag, null, null)
}
