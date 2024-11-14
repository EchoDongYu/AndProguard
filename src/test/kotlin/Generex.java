import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Generex {
    private final Random random = new Random();

    public String prepareRandom(String strMatch, State state, int minLength, int maxLength) {
        List<Transition> transitions = state.getSortedTransitions(false);
        Set<Integer> selectedTransitions = new HashSet<>();
        String result = strMatch;

        for (int resultLength = -1;
             transitions.size() > selectedTransitions.size()
                     && (resultLength < minLength || resultLength > maxLength);
             resultLength = result.length()) {

            if (randomPrepared(strMatch, state, minLength, maxLength, transitions)) {
                return strMatch;
            }

            int nextInt = random.nextInt(transitions.size());
            if (!selectedTransitions.contains(nextInt)) {
                selectedTransitions.add(nextInt);

                Transition randomTransition = transitions.get(nextInt);
                int diff = randomTransition.getMax() - randomTransition.getMin() + 1;
                int randomOffset = diff > 0 ? random.nextInt(diff) : diff;
                char randomChar = (char) (randomOffset + randomTransition.getMin());
                result = prepareRandom(strMatch + randomChar, randomTransition.getDest(), minLength, maxLength);
            }
        }

        return result;
    }

    private boolean randomPrepared(
            String strMatch,
            State state,
            int minLength,
            int maxLength,
            List<Transition> transitions) {

        if (state.isAccept()) {
            if (strMatch.length() == maxLength) {
                return true;
            }
            if (random.nextInt() > 0.3 * Integer.MAX_VALUE && strMatch.length() >= minLength) {
                return true;
            }
        }

        return transitions.isEmpty();
    }
}
