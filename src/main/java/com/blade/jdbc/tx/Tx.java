package com.blade.jdbc.tx;

import com.blade.jdbc.Base;
import com.blade.jdbc.exceptions.TransactionException;
import org.sql2o.Connection;

/**
 * Created by biezhi on 2017/3/15.
 */
public class Tx {

    private static final ThreadLocal<JdbcTx> jdbcTxThreadLocal = new ThreadLocal<>();

    public static void begin(Connection connection){
        jdbcTxThreadLocal.remove();
        jdbcTxThreadLocal.set(new JdbcTx(connection, true));
    }

    public static void begin(){
        begin(Base.sql2o.beginTransaction());
    }

    public static JdbcTx jdbcTx(){
        return jdbcTxThreadLocal.get();
    }

    public static Connection connection(){
        return jdbcTx().getConnection();
    }

    public static void rollback(){
        Connection connection = connection();
        if(null != connection){
            try {
                connection.rollback();
            } catch (Exception e){
                throw new TransactionException(e);
            } finally {
                connection.close();
            }
        }
    }

    public static void commit(){
        Connection connection = connection();
        if(null != connection){
            try {
                connection.commit(true);
            } catch (Exception e){
                throw new TransactionException(e);
            } finally {
                connection.close();
            }
        }
    }

    public static void close(){
        Connection connection = connection();
        if(null != connection){
            try {
                connection.close();
                jdbcTxThreadLocal.remove();
            } catch (Exception e){
                throw new TransactionException(e);
            }
        }
    }

}