package io.github.turskyi.travellingpro.models

import android.os.Parcel
import android.os.Parcelable
import com.chad.library.adapter.base.entity.node.BaseNode

data class City(var name: String, var parentId: Int, var month: String?) : BaseNode(), Parcelable {
    // required empty constructor for firestore serialization
    constructor() : this("", 0, null)

    constructor(name: String, parentId: Int) : this(
        name = name,
        parentId = parentId,
        null
    )

    constructor(parcel: Parcel) : this(
        parcel.readValue(String::class.java.classLoader) as String,
        parcel.readInt(),
        parcel.readString()
    )


    companion object CREATOR : Parcelable.Creator<City> {
        override fun createFromParcel(parcel: Parcel): City = City(parcel)
        override fun newArray(size: Int): Array<City?> = arrayOfNulls(size)
    }

    override val childNode: MutableList<BaseNode>?
        get() = null

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(name)
        parcel.writeInt(parentId)
        parcel.writeString(month)
    }

    override fun describeContents(): Int = 0
}