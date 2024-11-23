package com.murphy.util

object NamingCombo {
    val listVowels: MutableList<String> = ArrayList()
    val listConsonants: MutableList<String> = ArrayList()

    fun randomCombo(useVowels: Boolean?): String {
        return when (useVowels) {
            true -> listVowels
            false -> listConsonants
            null -> listVowels + listConsonants
        }.random()
    }

    fun initCombo(combinations: String) {
        listVowels.clear()
        listConsonants.clear()
        combinations.replace(Regex("\\W"), ",")
            .split(',')
            .distinct()
            .filter { it.isNotEmpty() }
            .forEach {
                val firstChar = it.first()
                if (naming_vowels.contains(firstChar, true)) {
                    listVowels.add(it)
                } else if (naming_consonants.contains(firstChar, true)) {
                    listConsonants.add(it)
                }
            }
    }
}