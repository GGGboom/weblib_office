package com.dcampus.common.config;



import org.hibernate.dialect.MySQL5Dialect;

/**
 * 数据库建表的编码格式
 */
public class MysqlConfig extends MySQL5Dialect {
    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }
}



