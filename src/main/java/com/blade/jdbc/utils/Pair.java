package com.blade.jdbc.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 * @date 2017/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pair<L, R> {

    private L left;
    private R right;

}
