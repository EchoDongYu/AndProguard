package com.murphy.util

import kotlin.random.Random

/**
 * 3~15
 */
fun randomWordLength(): Int {
    return when (Random.nextInt(1, 1000)) {
        in 1..20 -> 15 // 20
        in 20..41 -> 14 // 21
        in 41..104 -> 13 // 63
        in 104..197 -> 12 // 93
        in 197..314 -> 11 // 117
        in 314..445 -> 10 // 131
        in 445..577 -> 9 // 132
        in 577..697 -> 8 // 120
        in 697..794 -> 7 // 97
        in 794..868 -> 6 // 74
        in 868..921 -> 5 // 53
        in 921..967 -> 4 // 46
        else -> 3 // 33
    }
}