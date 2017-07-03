package com.blade.jdbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by biezhi on 03/07/2017.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhereParam {

    private String key;
    private String opt;
    private Object value;

}
