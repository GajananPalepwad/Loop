package com.gn4k.loop.models.response

import android.os.Parcel
import android.os.Parcelable

data class GetProjects(
    val message: String,
    val projects: List<Project>
)
data class Project(
    val project_id: Int,
    val title: String,
    val status: String,
    val description: String,
    val link_preview: String,
    val created_at: String,
    val joined_persons: List<ParticipantList>,
    val requested_people: List<ParticipantList>,
    val tags: List<String>,
    val author_id: Int,
    val updated_at: String
)



data class ParticipantList(
    val id: Int,
    val name: String,
    val photo_url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(photo_url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantList> {
        override fun createFromParcel(parcel: Parcel): ParticipantList {
            return ParticipantList(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantList?> {
            return arrayOfNulls(size)
        }
    }
}


