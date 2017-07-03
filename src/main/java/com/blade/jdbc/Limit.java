package com.blade.jdbc;

import java.util.function.Supplier;

/**
 * Created by biezhi on 03/07/2017.
 */
public class Limit implements Supplier<ConditionEnum> {

    private int page;
    private int limit;

    public Limit(int page, int limit) {
        this.page = page;
        this.limit = limit;
    }

    public static Limit of(int page, int limit) {
        return new Limit(page, limit);
    }

    @Override
    public ConditionEnum get() {
        return ConditionEnum.LIMIT;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

}
