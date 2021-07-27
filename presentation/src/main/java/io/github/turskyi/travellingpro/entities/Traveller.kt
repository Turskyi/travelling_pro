package io.github.turskyi.travellingpro.entities

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Traveller(
    val id: String,
    val name: String,
    val avatar: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(String::class.java.classLoader) as String,
        parcel.readValue(String::class.java.classLoader) as String,
        parcel.readValue(String::class.java.classLoader) as String,
    )

    companion object CREATOR : Parcelable.Creator<Traveller> {
        override fun createFromParcel(parcel: Parcel): Traveller {
            return Traveller(parcel)
        }

        override fun newArray(size: Int): Array<Traveller?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(avatar)
    }

}