package com.blade.jdbc.core;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Created by biezhi on 03/07/2017.
 */
public class Fields implements Supplier<ConditionEnum> {

    private Set<String> fields;

    public Fields(Set<String> fields) {
        this.fields = fields;
    }

    public static Fields of(String... fields) {
        return new Fields(new TreeSet<>(Arrays.asList(fields)));
    }

    public Set<String> getFields() {
        return fields;
    }

    @Override
    public ConditionEnum get() {
        return ConditionEnum.FIELDS;
    }
}
