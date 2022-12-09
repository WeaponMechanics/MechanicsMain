package me.deecaad.core.mechanics.inline;

import java.util.LinkedList;
import java.util.Map;

public class InlineSerializer {

    public static final Map<Character, Character> BRACKETS = Map.of('(', ')', '[', ']', '{', '}');

    public record OpenBracketData(char bracket, int index) {
    }


    public static void parse(String str, Map<String, Arguments> options) {
        LinkedList<OpenBracketData> stack = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            // Skip all whitespace since it doesn't matter
            if (isWhitespace(c))
                continue;

            if (BRACKETS.containsKey(c))
                stack.push(new OpenBracketData(c, i));

            if (!stack.isEmpty() && c == BRACKETS.get(stack.peek().bracket)) {
                OpenBracketData data = stack.pop();

            }
        }
    }

    public static OpenBracketData getBracket(char c) {
        if (BRACKETS.containsKey(c))
            return new OpenBracketData(BRACKETS.get(c), )
    }

    /**
     * Whitespace does not affect the inline-serialization process. All
     * whitespace is completely ignored. Want to use snake case? camel
     * case? Doesn't matter. Note that OUR definition of whitespace is
     * <b>very inaccurate</b>, see {@link Character#isWhitespace(char)}.
     *
     * @param c The character to check.
     * @return true if the character is a whitespace character.
     */
    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '-' || c == '_';
    }
}
