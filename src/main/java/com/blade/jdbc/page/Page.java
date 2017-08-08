package com.blade.jdbc.page;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页对象封装
 *
 * @author biezhi
 * @date 2017/7/24
 */
@Data
@ToString
public class Page<T> {

    private long    page;
    private long    prevPage;
    private long    nextPage;
    private long    totalPages;
    private long    totalRow;
    private List<T> rows;

    public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
        Page<R> page = new Page<>();
        page.setRows(rows.stream().map(mapper).collect(Collectors.toList()));
        page.setPage(this.page);
        page.setNextPage(this.nextPage);
        page.setPrevPage(this.prevPage);
        page.setTotalPages(this.totalPages);
        page.setTotalRow(this.totalRow);
        return page;
    }

    public boolean hasNext() {
        return this.nextPage > this.page;
    }

    public boolean hasPrev() {
        return this.prevPage < this.page;
    }

}
