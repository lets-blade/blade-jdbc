package com.blade.jdbc.core;

import java.util.function.Supplier;

/**
 * add limit condition
 * @author <a href="mailto:chenchen_839@126.com" target="_blank">ccqy66</a>
 */
public class Limit implements Supplier<ConditionEnum>{
    private long offset;
    private long row;
    private Limit(long offset,long row) {
        this.row = row;
        this.offset = offset;
    }
    public static Limit row(long row) {
        return new Limit(0,row);
    }
    public static Limit of(long offset,long row) {
        return new Limit(offset, row);
    }

    @Override
    public ConditionEnum get() {
        return ConditionEnum.LIMIT;
    }

    public long getOffset() {
        return offset;
    }

    public long getRow() {
        return row;
    }
}
