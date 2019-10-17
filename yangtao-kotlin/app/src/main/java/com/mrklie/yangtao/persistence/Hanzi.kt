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
    @ColumnInfo(name = "traditional") val traditional: String,
    @ColumnInfo(name = "pinyin") val pinyin: String,
    @ColumnInfo(name = "pinyin_numbered") val pinyinNumbered: String,
    @ColumnInfo(name = "definition") val definition: String,
    @ColumnInfo(name = "decomposition") val decomposition: String,
    @ColumnInfo(name = "origin") val origin: String,
    @ColumnInfo(name = "phonetic") val phonetic: String,
    @ColumnInfo(name = "semantic") val semantic: String,
    @ColumnInfo(name = "mnemonic") val mnemonic: String,
    @ColumnInfo(name = "etymology") val etymology: String,
    @ColumnInfo(name = "scanned") val scanned: Boolean = false
) : Parcelable