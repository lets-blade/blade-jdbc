package com.blade.jdbc.model;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * model converter config
 */
public class BeanConverterConfig {

    /**
     * 包含 BeanConverter 的 ContextClassLoader 实例索引
     */
    private static final ContextClassLoaderLocal BEANS_BY_CLASSLOADER = new ContextClassLoaderLocal() {

        // 创建默认的实例
        protected Object initialValue() {
            return new BeanConverterConfig();
        }
    };

    /**
     * 对象的转换器
     */
    private SoftReference<Map<Class<?>, TypeConverter>> converters = new SoftReference<Map<Class<?>, TypeConverter>>(
            new HashMap<Class<?>, TypeConverter>());

    /**
     * 获取实例，提供的功能应用于 {@link BeanConverter}.
     * 这是一个伪单例 - 每一个线程的ContextClassLoader提供一个单例的实例
     * 这种机制提供了在同一个web容器中部署的应用程序之间的隔离
     *
     * @return 该伪单例的实例 BeanConverterConfig
     */
    public static BeanConverterConfig getInstance() {
        BeanConverterConfig beanConverterConfig = (BeanConverterConfig) BEANS_BY_CLASSLOADER.get();
        return beanConverterConfig;
    }

    /**
     * 设置实例，提供的功能应用于 {@link BeanConverter}.
     * 这是一个伪单例 - 每一个线程的ContextClassLoader提供一个单例的实例
     * 这种机制提供了在同一个web容器中部署的应用程序之间的隔离
     *
     * @param newInstance 该伪单例的实例 BeanConverterConfig
     */
    public static void setInstance(BeanConverterConfig newInstance) {
        BEANS_BY_CLASSLOADER.set(newInstance);
    }

    /**
     * 注册转换器
     *
     * @param clazz     the clazz
     * @param converter the converter
     */
    public void registerConverter(Class<?> clazz, TypeConverter converter) {
        Map<Class<?>, TypeConverter> map = converters.get();
        if (map == null) {
            converters = new SoftReference<Map<Class<?>, TypeConverter>>(
                    map = new HashMap<Class<?>, TypeConverter>());
        }
        map.put(clazz, converter);
    }

    /**
     * 移除注册的转换器
     *
     * @param clazz the clazz
     */
    public void unregisterConverter(Class<?> clazz) {
        Map<Class<?>, TypeConverter> map = converters.get();
        if (map == null) {
            return;
        }
        map.remove(clazz);
    }

    /**
     * 获取所有转换器
     *
     * @return
     */
    public Map<Class<?>, TypeConverter> getConverters() {
        return converters.get();
    }

    /**
     * 清空注册的转换器
     */
    public void clearConverter() {
        this.converters.clear();
    }
}
