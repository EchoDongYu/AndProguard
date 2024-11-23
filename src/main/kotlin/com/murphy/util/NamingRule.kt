package com.murphy.util

import com.murphy.config.AndConfigState
import com.murphy.util.NamingCombo.randomCombo
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
    private val cumulativeProbability: Triple<Double, Double, Double>
        get() {
            val letterRatio = if (case != 0) letterWeight else 0.0
            val comboRatio = if (likeWord == 2) config.comboWeight else 0.0
            val digitRatio = if (digit) config.digitWeight else 0.0
            val underlineRatio = if (underline) config.underlineWeight else 0.0
            val total = letterRatio + comboRatio + digitRatio + underlineRatio
            val first = letterRatio / total
            val second = first + comboRatio / total
            return Triple(first, second, second + digitRatio / total)
        }

    val Char.nextUseVowels: Boolean?
        get() {
            val lowerChar = lowercaseChar()
            return when {
                likeWord <= 0 -> null
                naming_vowels.contains(lowerChar) -> false
                naming_consonants.contains(lowerChar) -> true
                else -> null
            }
        }

    fun generateRandomName(wordLength: Int): String {
        if (wordLength == 0) return ""
        val (letterCum, comboCum, digitCum) = cumulativeProbability
        val repeatable = (case == 0 && !digit && likeWord <= 0) || config.repeatFactor >= 1
        return buildString {
            fun checkAppend(char: Char): Boolean {
                return repeatable || char != lastOrNull() || Random.nextDouble() < config.repeatFactor
            }

            val firstChar = when (hump) {
                true -> naming_upper.random()
                false -> naming_lower.random()
                null -> null
            }
            firstChar?.let { append(it) }
            var useVowels = firstChar?.nextUseVowels
            while (length < wordLength) {
                val lucky = Random.nextDouble()
                when {
                    lucky < letterCum -> {
                        val nextChar = when (useVowels) {
                            true -> naming_vowels
                            false -> naming_consonants
                            null -> naming_lower
                        }.random()
                        if (checkAppend(nextChar)) {
                            useVowels = nextChar.nextUseVowels
                            val upper = when {
                                case == 1 -> true
                                case == 2 -> false
                                lucky < letterCum / 2 -> true
                                else -> false
                            }
                            append(if (upper) nextChar.uppercaseChar() else nextChar)
                        }
                    }

                    lucky < comboCum -> {
                        val combo = randomCombo(useVowels)
                        if (checkAppend(combo.first()) && combo.length <= wordLength - length) {
                            useVowels = combo.last().nextUseVowels
                            val nextString = when (case) {
                                1 -> combo.uppercase()
                                2 -> combo.lowercase()
                                3 -> String(CharArray(combo.length) {
                                    if (Random.nextBoolean()) combo[it].uppercaseChar()
                                    else combo[it].lowercaseChar()
                                })

                                else -> combo
                            }
                            append(nextString)
                        }
                    }

                    else -> {
                        val nextChar = if (lucky < digitCum) naming_digit.random()
                        else naming_underline
                        if (checkAppend(nextChar)) append(nextChar)
                    }
                }
            }
        }
    }

    companion object {
        private val config by lazy { AndConfigState.getInstance() }
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