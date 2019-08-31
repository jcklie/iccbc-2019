package com.mrklie.yangtao.hanzigrid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.random.Random

@Parcelize
data class HanziModel(val character: String, val pinyin: String, val scanned: Boolean = Random.nextBoolean())  : Parcelable