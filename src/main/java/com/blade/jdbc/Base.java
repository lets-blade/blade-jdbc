package com.blade.jdbc;

import com.blade.jdbc.dialect.*;
import com.blade.jdbc.page.PageRow;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.function.Supplier;

@Slf4j
public final class Base {

    public static final ThreadLocal<PageRow>    pageLocal             = new ThreadLocal<>();
    public static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public static Sql2o   sql2o;
    public static Dialect dialect;

    public static Sql2o open(String url, String user, String password) {
        sql2o = new Sql2o(url, user, password);
        initSql2o();
        return sql2o;
    }

    public static Sql2o open(DataSource dataSource) {
        sql2o = new Sql2o(dataSource);
        initSql2o();
        return sql2o;
    }

    public static Sql2o open(Sql2o sql2o_) {
        sql2o = sql2o_;
        initSql2o();
        return sql2o;
    }

    private static void initSql2o() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            dialect = new MySQLDialect();
            log.info("⬢ Using Database: MySQL");
        } catch (Exception e) {
        }
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            dialect = new OracleDialect();
            log.info("⬢ Using Database: Oracle");
        } catch (Exception e) {
        }
        try {
            Class.forName("org.postgresql.Driver");
            dialect = new PostgreDialect();
            log.info("⬢ Using Database: PostgreSQL");
        } catch (Exception e) {
        }
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            dialect = new Db2Dialect();
            log.info("⬢ Using Database: DB2");
        } catch (Exception e) {
        }
        log.info("⬢ Blade-JDBC initializing");
    }

    /**
     * 原子提交
     *
     * @param supplier
     * @param <T>
     */
    public static <T> T atomic(Supplier<T> supplier) {
        T result = null;
        try (Connection connection = sql2o.beginTransaction()) {
            connectionThreadLocal.set(connection);
            result = supplier.get();
            connection.commit();
        } catch (Exception e) {
            log.error("Transaction rollback", e);
        } finally {
            connectionThreadLocal.remove();
            return result;
        }
    }

}