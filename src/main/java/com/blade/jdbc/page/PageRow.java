package com.blade.jdbc.page;

import lombok.Data;

@Data
public class PageRow {

    private int page;
    private int offset;
    private int limit;

    public PageRow(int page, int limit) {
        this.page = page;
        this.offset = (page - 1) * limit;
        this.limit = limit;
    }

}