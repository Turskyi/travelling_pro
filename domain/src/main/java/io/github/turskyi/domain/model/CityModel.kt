package io.github.turskyi.domain.model

data class CityModel(
    var id: Int?,
    var name: String,
    var parentId: Int,
    var month: String?
)
