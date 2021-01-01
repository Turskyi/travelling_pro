package io.github.turskyi.travellingpro.models

import com.chad.library.adapter.base.entity.node.BaseNode

class City( var name: String, var parentId: Int, var month: String?) : BaseNode() {
    /* required empty constructor for firestore serialization */
    constructor() : this( "", 0, null)

    constructor(name: String, parentId: Int) : this(
        name = name,
        parentId = parentId,
        null
    )

    override val childNode: MutableList<BaseNode>?
        get() = null
}