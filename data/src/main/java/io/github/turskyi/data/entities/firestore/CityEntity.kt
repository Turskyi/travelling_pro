package io.github.turskyi.data.entities.firestore

data class CityEntity(
        var id: Int?,
       val name: String,
       val parentId: Int,
       val month: String?
)
