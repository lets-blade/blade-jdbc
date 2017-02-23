package com.blade.jdbc.utils;

import com.blade.jdbc.model.PageRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class Utils {

    /**
     * Returns true if value is either null or it's String representation is blank.
     *
     * @param value object to check.
     * @return true if value is either null or it's String representation is blank, otherwise returns false.
     */
    public static boolean blank(Object value) {
        return value == null || value.toString().trim().length() == 0;
    }

    /**
     * Returns true if array is either null or empty.
     *
     * @param array array to check
     * @return true if array is either null or empty, false otherwise
     */
    public static boolean empty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns true if collection is either null or empty.
     *
     * @param collection collection to check
     * @return true if collection is either null or empty, false otherwise
     */
    public static boolean empty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Joins the items in array with a delimiter.
     *
     * @param array     array of items to join.
     * @param delimiter delimiter to insert between elements of array.
     * @return string with array elements separated by delimiter. There is no trailing delimiter in the string.
     */
    public static String join(String[] array, String delimiter) {
        if (empty(array)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        join(sb, array, delimiter);
        return sb.toString();
    }

    public static String join(String[] strs) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(strs[i]);
        }
        return buf.toString();
    }

    public static String getInSql(int len) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buf.append(",?");
        }
        return "(" + buf.substring(1) + ")";
    }

    public static String join(Object[] strs) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(strs[i]);
        }
        return buf.toString();
    }

    public static String join(List<String> strs) {
        StringBuilder buf = new StringBuilder();
        int len = strs.size();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(strs.get(i));
        }
        return buf.toString();
    }

    public static String getQuestionMarks(int count) {
        StringBuilder sb = new StringBuilder(count * 2);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    /**
     * Splits a string into an array using provided delimiters. Empty (but not blank) split chunks are omitted.
     * The split chunks are trimmed.
     *
     * @param input      string to split.
     * @param delimiters delimiters
     * @return a string split into an array using provided delimiters
     */
    public static String[] split(String input, String delimiters) {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        }

        List<String> tokens = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(input, delimiters);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken().trim());
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Splits a string into an array using provided delimiter. Empty (but not blank) split chunks are omitted.
     * The split chunks are trimmed.
     *
     * @param input     string to split.
     * @param delimiter delimiter
     * @return a string split into an array using a provided delimiter
     */
    public static String[] split(String input, char delimiter) {
        return split(input, String.valueOf(delimiter));
    }

    /**
     * Joins the items in collection with a delimiter.
     *
     * @param collection collection of items to join.
     * @param delimiter  delimiter to insert between elements of collection.
     * @return string with collection elements separated by delimiter. There is no trailing delimiter in the string.
     */
    public static String join(Collection<?> collection, String delimiter) {
        if (collection.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        join(sb, collection, delimiter);
        return sb.toString();
    }

    /**
     * Joins the items in collection with a delimiter, and appends the result to StringBuilder.
     *
     * @param sb         StringBuilder to append result to
     * @param collection collection of items to join.
     * @param delimiter  delimiter to insert between elements of collection.
     */
    public static void join(StringBuilder sb, Collection<?> collection, String delimiter) {
        if (collection.isEmpty()) {
            return;
        }
        Iterator<?> it = collection.iterator();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
        }
    }

    /**
     * Joins the items in array with a delimiter, and appends the result to StringBuilder.
     *
     * @param sb        StringBuilder to append result to
     * @param array     array of items to join.
     * @param delimiter delimiter to insert between elements of array.
     */
    public static void join(StringBuilder sb, Object[] array, String delimiter) {
        if (empty(array)) {
            return;
        }
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(delimiter);
            sb.append(array[i]);
        }
    }

    /**
     * Repeats string of characters a defined number of times, and appends result to StringBuilder.
     *
     * @param sb    StringBuilder to append result to
     * @param str   string of characters to be repeated.
     * @param count number of times to repeat, zero or a negative number produces no result
     */
    public static void repeat(StringBuilder sb, String str, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
    }

    public static boolean isPrimitiveOrString(Class<?> c) {
        if (c.isPrimitive()) {
            return true;
        } else if (c == Byte.class
                || c == Short.class
                || c == Integer.class
                || c == Long.class
                || c == Float.class
                || c == Double.class
                || c == Boolean.class
                || c == Character.class
                || c == String.class) {
            return true;
        } else {
            return false;
        }
    }

    public static void closeQuietly(AutoCloseable autoCloseable) {
        try {
            if (autoCloseable != null) {
                autoCloseable.close();
            }
        } catch (Exception ignore) {
        }
    }


    /**
     * 获取总数sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql
     * @return
     */
    public static String getCountSql(String sql) {
        return "select count(0) from (" + sql + ") tmp_count";
    }

    /**
     * 获取分页查询sql
     *
     * @param sql
     * @param pageRow
     * @return
     */
    public static String getPageSql(String sql, String dialect, PageRow pageRow) {
        StringBuilder pageSql = new StringBuilder(200);
        if ("mysql".equalsIgnoreCase(dialect)) {
            pageSql.append(sql);
            if (StringUtils.isNotBlank(pageRow.getOrderBy())) {
                pageSql.append(" order by ");
                pageSql.append(pageRow.getOrderBy());
            }
            pageSql.append(" limit ");
            pageSql.append(pageRow.getOffSet());
            pageSql.append(",");
            pageSql.append(pageRow.getLimit());
        } else if ("oracle".equalsIgnoreCase(dialect)) {
            pageSql.append("select * from ( select rownum num,temp.* from (");
            pageSql.append(sql);
            if (StringUtils.isNotBlank(pageRow.getOrderBy())) {
                pageSql.append(" order by ");
                pageSql.append(pageRow.getOrderBy());
            }
            pageSql.append(") temp where rownum <= ").append(pageRow.getLimit());
            pageSql.append(") where num > ").append(pageRow.getOffSet());
        }
        return pageSql.toString();
    }


}
