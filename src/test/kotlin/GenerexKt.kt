import dk.brics.automaton.State
import dk.brics.automaton.Transition
import java.util.HashSet
import java.util.Random

class GenerexKt {
    private val random = Random()

    fun prepareRandom(strMatch: String, state: State, minLength: Int, maxLength: Int): String {
        val transitions = state.getSortedTransitions(false)
        val selectedTransitions: MutableSet<Int> = HashSet<Int>()
        var result = strMatch

        var resultLength = -1
        while (transitions.size > selectedTransitions.size
            && (resultLength < minLength || resultLength > maxLength)
        ) {
            if (randomPrepared(strMatch, state, minLength, maxLength, transitions)) {
                return strMatch
            }

            val nextInt = random.nextInt(transitions.size)
            if (!selectedTransitions.contains(nextInt)) {
                selectedTransitions.add(nextInt)

                val randomTransition = transitions[nextInt]
                val diff = randomTransition.getMax().code - randomTransition.getMin().code + 1
                val randomOffset = if (diff > 0) random.nextInt(diff) else diff
                val randomChar = (randomOffset + randomTransition.getMin().code).toChar()
                result = prepareRandom(strMatch + randomChar, randomTransition.dest, minLength, maxLength)
            }
            resultLength = result.length
        }

        return result
    }

    private fun randomPrepared(
        strMatch: String,
        state: State,
        minLength: Int,
        maxLength: Int,
        transitions: MutableList<Transition>
    ): Boolean {
        if (state.isAccept) {
            if (strMatch.length == maxLength) {
                return true
            }
            if (random.nextInt() > 0.3 * Int.Companion.MAX_VALUE && strMatch.length >= minLength) {
                return true
            }
        }

        return transitions.isEmpty()
    }
}
