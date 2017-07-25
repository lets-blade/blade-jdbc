package com.blade.jdbc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Utils {

    public static boolean empty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Splits a string into an array using provided delimiters. Empty (but not blank) split chunks are omitted.
     * The split chunks are trimmed.
     *
     * @param input      string to split.
     * @param delimiters delimiters
     * @return a string split into an array using provided delimiters
     */
    public static String[] split(String input, String delimiters) {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        }

        List<String> tokens = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(input, delimiters);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken().trim());
        }
        return tokens.toArray(new String[tokens.size()]);
    }

}
