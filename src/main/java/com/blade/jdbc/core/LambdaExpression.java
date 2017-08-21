package com.blade.jdbc.core;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author biezhi
 * @date 2017/8/21
 */
@FunctionalInterface
public interface LambdaExpression<T, R> extends Function<T, R>, Serializable {

}
