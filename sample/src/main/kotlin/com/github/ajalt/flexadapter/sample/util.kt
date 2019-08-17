package com.github.ajalt.flexadapter.sample

import android.graphics.Color
import kotlin.random.Random

fun randomColor(): Int {
    return Color.HSVToColor(floatArrayOf(Random.nextFloat() * 360, 75f, 80f))
}
