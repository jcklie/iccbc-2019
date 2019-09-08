package com.mrklie.yangtao.util

import android.graphics.Color
import com.mrklie.yangtao.R

val FIRST_TONES = setOf('ā', 'ē', 'ī', 'ō', 'ū')
val SECOND_TONES = setOf('á', 'é', 'í', 'ó', 'ḿ', 'ú')
val THIRD_TONES = setOf('ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ')
val FOURTH_TONES = setOf('à', 'è', 'ì', 'ò', 'ù')

fun getTone(pinyin: String): Int {
    val s = pinyin.toSet()
    return when {
        s.any { FIRST_TONES.contains(it)} -> 1
        s.any { SECOND_TONES.contains(it)} -> 2
        s.any { THIRD_TONES.contains(it)} -> 3
        s.any { FOURTH_TONES.contains(it)} -> 4
        else -> 5
    }
}

fun getColorForHanzi(pinyin: String): Int {
    val tone = getTone(pinyin)
    return when (tone) {
        1 -> R.color.firstTone
        2 -> R.color.secondTone
        3 -> R.color.thirdTone
        4 -> R.color.fourthTone
        5 -> R.color.fifthTone
        else -> R.color.colorBlack
    }
}