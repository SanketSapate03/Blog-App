package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable

data class BlogItemModel(
    var heading:String?="null",
    val userName:String?="null",
    val date:String?="null",
    var post:String?="null",
    val userId:String?="null",
    var likeCount:Int=0,
    val profileImageUrl:String?="null",
    var postId:String="null",
    var isSaved:Boolean=false,
    val likedBy:MutableList<String>?=null,

    ):Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readInt(),
        parcel.readString()?:"null",
        parcel.readString()?:"null",
        parcel.readByte()!=0.toByte()

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(heading)
        parcel.writeString(userName)
        parcel.writeString(date)
        parcel.writeString(post)
        parcel.writeString(userId)
        parcel.writeInt(likeCount)
        parcel.writeString(profileImageUrl)
        parcel.writeString(postId)
        parcel.writeByte(if (isSaved)1 else 0)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BlogItemModel> {
        override fun createFromParcel(parcel: Parcel): BlogItemModel {
            return BlogItemModel(parcel)
        }

        override fun newArray(size: Int): Array<BlogItemModel?> {
            return arrayOfNulls(size)
        }
    }

}
