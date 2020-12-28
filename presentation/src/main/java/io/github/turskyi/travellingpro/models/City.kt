package io.github.turskyi.travellingpro.models

import com.chad.library.adapter.base.entity.node.BaseNode

class City(var id: Int?, var name: String, var parentId: Int, var month: String?) : BaseNode() {
    constructor(name: String, parentId: Int, month: String) : this(
        null,
        name = name,
        parentId = parentId,
        month = month
    )

    constructor(name: String, parentId: Int) : this(
        null,
        name = name,
        parentId = parentId,
        null
    )

    override val childNode: MutableList<BaseNode>?
        get() = null
}