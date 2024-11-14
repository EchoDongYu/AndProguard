package com.murphy.util

import kotlin.random.Random

/**
 * 命名规则 C/S, U/L/I, D, _, W1/2
 * 示例 [^CLD_W2]
 *
 * @property hump true->Capital大驼峰, false->Small小驼峰
 * @property case 0->NoCase, 1->UpperCase, 2->LowerCase, 3->IgnoreCase
 * @property digit 是/否含数字
 * @property underline 是/否含下划线
 * @property likeWord 是/否模仿单词, v1（元辅音交替）v2（添加常见组合）
 */
data class NamingRule(
    val hump: Boolean?,
    val case: Int,
    val digit: Boolean,
    val underline: Boolean,
    val likeWord: Int
) {
    private val cumulate: Pair<Double, Double>
        get() {
            val letterProp = if (case != 0) 1.0 else 0.0
            val digitProp = if (digit) digitWeight else 0.0
            val underlineProp = if (underline) underlineWeight else 0.0
            val total = letterProp + digitProp + underlineProp
            val letterFrequency = letterProp / total
            return Pair(letterFrequency, letterFrequency + digitProp / total)
        }

    fun generateRandomName(length: Int): String {
        if (likeWord > 0 && length > 1) return fakeWord(length)
        return when (hump) {
            true -> naming_upper.random() + randomWord(length - 1)
            false -> naming_lower.random() + randomWord(length - 1)
            null -> randomWord(length)
        }
    }

    private fun randomWord(length: Int): String {
        if (length == 0) return ""
        val (letterCum, digitCum) = cumulate
        return buildString {
            repeat(length) {
                val randomDouble = Random.nextDouble()
                val randomChar = when {
                    randomDouble < letterCum -> when {
                        case == 1 -> naming_upper
                        case == 2 -> naming_lower
                        randomDouble < letterCum / 2 -> naming_upper
                        else -> naming_lower
                    }

                    randomDouble < digitCum -> naming_digit
                    else -> naming_underline
                }.random()
                append(randomChar)
            }
        }
    }

    private fun fakeWord(length: Int): String {
        return ""
    }
}

fun parseRuleOrThrow(content: String): NamingRule {
    return parseRule(content) ?: throw IllegalArgumentException("Wrong naming rule: $content")
}

private fun parseRule(content: String): NamingRule? {
    val capital = content.contains('C', true)
    val small = content.contains('S', true)
    val hump = when {
        capital && !small -> true
        !capital && small -> false
        !capital && !small -> null
        else -> return null
    }

    val uppercase = content.contains('U', true)
    val lowercase = content.contains('L', true)
    val ignoreCase = content.contains('I', true)
    val case = when {
        ignoreCase || (uppercase && lowercase) -> 3
        lowercase -> 2
        uppercase -> 1
        else -> 0
    }

    val digit = content.contains('D', true)
    val underline = content.contains('_')
    val regex = Regex("[Ww](\\d)")
    val matches = regex.findAll(content)
    val results = matches.map { it.groupValues[1] }.toList()
    if (results.size > 1) return null
    val likeWord = results.firstOrNull()?.toInt() ?: 0
    if (likeWord !in 0..2) return null
    return NamingRule(hump, case, digit, underline, likeWord)
}