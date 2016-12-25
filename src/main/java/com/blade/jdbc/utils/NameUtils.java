package com.blade.jdbc.utils;

/**
 * 名称操作辅助类
 */
public class NameUtils {

    /**
     * 首字母大写
     * 
     * @param name
     * @return
     */
    public static String getFirstUpperName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String firstChar = name.substring(0, 1).toUpperCase();
        return firstChar + name.substring(1);
    }

    /**
     * 首字母小写
     * 
     * @param name
     * @return
     */
    public static String getFirstLowerName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String firstChar = name.substring(0, 1).toLowerCase();
        return firstChar + name.substring(1);
    }

    /**
     * 转换成骆驼命名法返回
     * 
     * @param name
     * @return
     */
    public static String getCamelName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        name = name.toLowerCase();
        //去掉前面的_
        while (name.startsWith("_")) {
            name = name.substring(1);
        }
        //去掉后面的_
        while (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {

            char c = name.charAt(i);

            if (c == '_') {
                i++;
                sb.append(Character.toUpperCase(name.charAt(i)));
                continue;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * 将骆驼命名法反转成下划线返回
     * 
     * @param name
     * @return
     */
    public static String getUnderlineName(String name) {

        if (StringUtils.isBlank(name)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {

            char c = name.charAt(i);

            if (i > 0 && Character.isUpperCase(c)) {
                sb.append("_");
            }

            sb.append(c);
        }

        return sb.toString().toLowerCase();
    }

    /**
     * 保留原文件后缀生成唯一文件名
     *
     * @param fileName
     * @return
     */
    public static String createUniqueFileName(String fileName) {

        int index = fileName.lastIndexOf(".");
        String suffix = fileName.substring(index);
        String uqName = UUIDUtils.getUUID16() + suffix;
        return uqName;
    }

    /**
     * 在文件名后加上指定后缀，不包括后缀名
     * 
     * @param fileName
     * @param endSuffix
     * @return
     */
    public static String createEndSuffixFileName(String fileName, String endSuffix) {
        int index = fileName.lastIndexOf(".");
        String preFileName = fileName.substring(0, index);
        String suffix = fileName.substring(index);
        return preFileName + endSuffix + suffix;
    }

}
