package io.github.turskyi.travellingpro.models

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

data class VisitedCountryNode(
    var id: Int,
    var flag: String,
    var selfie: String?,
    var selfieName: String?,
    override var childNode: MutableList<BaseNode>? = null,
    val title: String,
) : BaseExpandNode()
