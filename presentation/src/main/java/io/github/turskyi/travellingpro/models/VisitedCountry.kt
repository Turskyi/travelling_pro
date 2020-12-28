package io.github.turskyi.travellingpro.models

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class VisitedCountry(
    var id: Int,
    var img: String,
    var visited: Boolean?,
    var selfie: String?,
    override var childNode: MutableList<BaseNode>? = null, val title: String) : BaseExpandNode()
