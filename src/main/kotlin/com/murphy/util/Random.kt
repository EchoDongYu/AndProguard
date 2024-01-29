package com.murphy.util

import kotlin.random.Random

val randomLetterIndex get() = Random.nextInt(0, 26)

/**
 * 降低重复出现的概率
 */
fun Int.randomLetterIndex(): Int {
    if (Random.nextInt(0, 5) == 0) return randomLetterIndex
    val random = Random.nextInt(0, 25)
    return if (random >= this) random + 1 else random
}

fun splitPosArray(length: Int, split: Int): Array<Int> = if (split > 0) {
    val step = length / (split + 1) / 2
    val flag = Random.nextBoolean()
    Array(split) {
        val index = (it + 1) * length / (split + 1)
        if (flag) Random.nextInt(index - step, index)
        else Random.nextInt(index, index + step)
    }
} else arrayOf()

private fun defaultSplit(from: Int, until: Int, length: Int): Int {
    val median = (from + until - 1) / 2F
    return if (length < median) 0
    else if (length < (median + until) / 2F) 1
    else Random.nextInt(1, 3)
}

fun randomName(
    from: Int, until: Int,
    upperStart: Boolean = false,
    underline: Boolean = false,
    onSplit: (Int) -> Int = { defaultSplit(from, until, it) }
): String {
    val length = Random.nextInt(from, until)
    val split = if (length > 0) onSplit(length) else 0
    val posArray: Array<Int> = splitPosArray(length, split)
    var lastIndex = randomLetterIndex
    val strBuilder = StringBuilder()
    strBuilder.append(if (upperStart) UPPERCASE_LETTER[lastIndex] else LOWERCASE_LETTER[lastIndex])
    for (i in 0 until length) {
        lastIndex = lastIndex.randomLetterIndex()
        val letter = if (posArray.contains(i).not()) LOWERCASE_LETTER[lastIndex]
        else if (underline) "_" else UPPERCASE_LETTER[lastIndex]
        strBuilder.append(letter)
    }
    return strBuilder.toString()
}

val randomClassName get() = randomName(5, 16, upperStart = true)

val randomFieldName get() = randomName(2, 12)

val randomMethodName get() = randomName(5, 20)

val randomResIdName
    get() = randomName(7, 16, underline = true) { if (it < 13) 1 else 2 }

val randomResFileName
    get() = randomName(15, 18, underline = true) { 2 }