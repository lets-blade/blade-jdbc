package com.blade.jdbc;

/**
 * Created by biezhi on 03/07/2017.
 */
public class OrderBy {

    private String orderBy;

    private OrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public static OrderBy asc(String field) {
        return new OrderBy(" order by " + field + " asc");
    }

    public static OrderBy desc(String field) {
        return new OrderBy(" order by " + field + " desc");
    }

    public static OrderBy order(String orderBy) {
        return new OrderBy(" order by " + orderBy);
    }

    public String getOrderBy() {
        return this.orderBy;
    }

}
