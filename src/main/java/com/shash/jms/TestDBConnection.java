package com.shash.jms;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {

    private HikariConfig config;
    private HikariDataSource ds;

    public TestDBConnection(){
        System.out.println("inside const");
        config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        config.setUsername("root");
        config.setPassword("password");
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(15);
        config.setIdleTimeout(600000);
        config.setKeepaliveTime(600000);
        config.setMaxLifetime(180000);
        config.setConnectionTimeout(2000);
        config.setInitializationFailTimeout(1000);
        config.setConnectionInitSql("select 1 from dual");
        config.setValidationTimeout(2000);
        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void main(String[] args){
        TestDBConnection dbConnection = new TestDBConnection();
        try {
            Connection connection = dbConnection.getConnection();
            System.out.println("working");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("hello db");
    }
}
