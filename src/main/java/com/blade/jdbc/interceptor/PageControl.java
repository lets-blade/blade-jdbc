package com.blade.jdbc.interceptor;

import com.blade.jdbc.model.PageRow;
import com.blade.jdbc.model.Paginator;
import com.blade.jdbc.utils.Utils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * 分页拦截器
 */
@Aspect
@Component
//这里使用注解，也可以在配置文件里声明bean，一样
public class PageControl {

    /** 分页线程变量 */
    public static final ThreadLocal<PageRow>    LOCAL_PAGER     = new ThreadLocal<>();

    /** 获取总记录数 */
    private static final ThreadLocal<Boolean> GET_ITEMS_TOTAL = new ThreadLocal<>();

    /** 数据库 */
    public static String                      DATABASE;

    /**
     * 执行分页
     */
    public static void performPage(int page, int limit) {
        performPage(new PageRow(page, limit), true);
    }

    /**
     * 执行分页
     */
    public static void performPage(PageRow pageRow, boolean isGetCount) {
        GET_ITEMS_TOTAL.set(isGetCount);
        LOCAL_PAGER.set(pageRow);
    }

    /**
     * 获取Pager对象
     * 
     * @return
     */
    public static PageRow getPager() {
        PageRow pageRow = LOCAL_PAGER.get();
        //获取数据时清除
        LOCAL_PAGER.remove();
        GET_ITEMS_TOTAL.remove();
        return pageRow;
    }

    @Pointcut("execution(* org.springframework.jdbc.core.JdbcTemplate.query*(..))")
    public void queryMethod() {
        //该方法没实际作用，只是切面声明对象，声明一个切面的表达式
        //下面使用时，只需要写入这个表达式名(方法名)即可   等同于
        //@Before("anyMethod()") == @Before("execution(* com.liyd.sample.service.impl.*.*(..))")
        //可以是private修饰符，但是会有never used的警告，所以这里用了public
    }

    @Around("queryMethod()")
    public Object pagerAspect(ProceedingJoinPoint pjp) throws Throwable {

        if (LOCAL_PAGER.get() == null) {
            return pjp.proceed();
        }

        JdbcTemplate target = (JdbcTemplate) pjp.getTarget();
        if (DATABASE == null) {
            DatabaseMetaData metaData = target.getDataSource().getConnection().getMetaData();
            DATABASE = metaData.getDatabaseProductName().toUpperCase();
        }

        Object[] args = pjp.getArgs();
        String querySql = (String) args[0];
        PageRow pageRow = LOCAL_PAGER.get();
        args[0] = Utils.getPageSql(querySql, DATABASE, pageRow);

        Paginator paginator =null;
        if (GET_ITEMS_TOTAL.get()) {
            String countSql = Utils.getCountSql(querySql);
            Object[] countArgs = null;
            for (Object obj : args) {
                if (obj instanceof Object[]) {
                    countArgs = (Object[]) obj;
                }
            }
            int itemsTotal = target.queryForInt(countSql, countArgs);
            paginator = new Paginator<>(itemsTotal, pageRow.getPage(), pageRow.getLimit());
        }

        Object result = pjp.proceed(args);
        if(null != result){
            paginator.setList((List<?>) result);
        }
        return paginator;
    }

}
