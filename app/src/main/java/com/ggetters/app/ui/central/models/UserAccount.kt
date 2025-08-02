package com.ggetters.app.ui.central.models

import android.os.Parcel
import android.os.Parcelable

data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String?,
    val teamName: String,
    val role: String,
    val isActive: Boolean
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(avatar)
        parcel.writeString(teamName)
        parcel.writeString(role)
        parcel.writeByte(if (isActive) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<UserAccount> {
        override fun createFromParcel(parcel: Parcel): UserAccount =
            UserAccount(parcel)
        override fun newArray(size: Int): Array<UserAccount?> =
            arrayOfNulls(size)
    }
}