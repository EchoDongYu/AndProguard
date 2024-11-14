package com.murphy.util

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import kotlin.random.Random

private const val minLength = 1
private const val maxLength = 500

val Automaton.randomString: String
    get() {
        val stack = mutableListOf("" to initialState)

        while (stack.isNotEmpty()) {
            val (currentStr, currentState) = stack.removeAt(stack.lastIndex)
            val transitions = currentState.getSortedTransitions(false).shuffled()
            if (isRandomlyAccepted(currentStr, currentState) || transitions.isEmpty())
                return currentStr
            for (transition in transitions) {
                val randomChar = transition.run { min.code + Random.nextInt(max.code - min.code + 1) }.toChar()
                val nextStr = currentStr + randomChar
                if (nextStr.length in minLength..maxLength) {
                    stack.add(nextStr to transition.dest)
                }
            }
        }

        throw IllegalArgumentException("Unable to generate a valid string for the given automaton: $this")
    }

private fun isRandomlyAccepted(str: String, state: State): Boolean {
    if (state.isAccept) {
        if (str.length >= maxLength) return true
        if (str.length >= minLength && Random.nextDouble() < 0.3) return true
    }
    return false
}