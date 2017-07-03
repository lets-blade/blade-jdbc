package com.blade.jdbc;

import java.util.function.Supplier;

/**
 * Created by biezhi on 03/07/2017.
 */
public class OrderBy implements Supplier<ConditionEnum> {

    private String orderBy;

    private OrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public static OrderBy asc(String field) {
        return new OrderBy(field + " asc");
    }

    public static OrderBy desc(String field) {
        return new OrderBy(field + " desc");
    }

    public static OrderBy of(String orderBy) {
        return new OrderBy(orderBy);
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    @Override
    public ConditionEnum get() {
        return ConditionEnum.ORDER_BY;
    }
}
