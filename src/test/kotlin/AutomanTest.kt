import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import java.lang.StringBuilder
import kotlin.random.Random

private const val minLength = 1
private const val maxLength = 500

val Automaton.randomStringFirst: String
    get() {
        val result = StringBuilder()
        var state = initialState
        var count = 1
        while (!isRandomlyAccepted(result, state, count)) {
            if (state.isAccept) count++
            val transitions = state.transitions
            if (transitions.isEmpty()) break
            val transition = transitions.random()
            val randomChar = transition.run { min.code + Random.nextInt(max.code - min.code + 1) }.toChar()
            result.append(randomChar)
            state = transition.dest
        }
        return result.toString()
    }

private fun isRandomlyAccepted(content: CharSequence, state: State, count: Int): Boolean {
    if (state.isAccept) {
        if (content.length >= maxLength) return true
        if (content.length >= minLength && Random.nextDouble() < 1 / (6.0 - count)) return true
    }
    return false
}
