package io.github.turskyi.travellingpro.models

import android.os.Parcel
import android.os.Parcelable

data class Country(
    var id: Int,
    val name: String,
    val flag: String,
    var visited: Boolean,
    var selfie: String,
    val selfieName: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as Int,
        parcel.readValue(String::class.java.classLoader) as String,
        parcel.readValue(String::class.java.classLoader) as String,
        parcel.readValue(Boolean::class.java.classLoader) as Boolean,
        parcel.readValue(String::class.java.classLoader) as String,
        parcel.readValue(String::class.java.classLoader) as String
    )

    companion object CREATOR : Parcelable.Creator<Country> {
        override fun createFromParcel(parcel: Parcel): Country = Country(parcel)
        override fun newArray(size: Int): Array<Country> = emptyArray()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(name)
        parcel.writeValue(flag)
        parcel.writeValue(visited)
        parcel.writeValue(selfie)
        parcel.writeValue(selfieName)
    }

    override fun describeContents() = 0
}