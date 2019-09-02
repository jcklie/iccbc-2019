package com.mrklie.yangtao.persistence

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "hanzi")
data class Hanzi(
    @PrimaryKey @ColumnInfo(name = "hanzi") val character: String,
    @ColumnInfo(name = "pinyin") val pinyin: String,
    @ColumnInfo(name = "etymology") val etymology: String,
    @ColumnInfo(name = "scanned") val scanned: Boolean = false
) : Parcelable