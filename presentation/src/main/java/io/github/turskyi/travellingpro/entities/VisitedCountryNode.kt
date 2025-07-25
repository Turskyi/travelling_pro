package io.github.turskyi.travellingpro.entities

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

data class VisitedCountryNode(
    val id: Int,
    val shortName: String,
    val title: String,
    val flag: String,
    val selfie: String,
    val selfieName: String,
    override var childNode: MutableList<BaseNode>,
) : BaseExpandNode() {
    constructor(id: Int, shortName: String, title: String, flag: String, selfie: String, selfieName: String) : this(
        id = id,
        shortName = shortName,
        title = title,
        flag = flag,
        selfie = selfie,
        selfieName = selfieName,
        childNode = mutableListOf(),
    )
}
