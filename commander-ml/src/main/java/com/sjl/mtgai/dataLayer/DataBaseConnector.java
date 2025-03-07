package com.sjl.mtgai.dataLayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseConnector {

    private String PDURL = "jdbc:postgresql://localhost:5432/capstone_mtgtml";
    private String username = "postgres";
    private String password = "S93nc3rL*";
    private  Connection connection;

    public Connection getConnection() throws SQLException {
        connection = DriverManager.getConnection(PDURL, username, password);
        return connection;
    }

    public void closeConnection() throws SQLException{
        connection.close();
    }

    public ResultSet select(String column, String table) throws SQLException {
        Statement SELECT = connection.createStatement();
        return SELECT.executeQuery("SELECT " + column + " FROM " + table);

    }

    public ResultSet query(String statement) throws SQLException {
        Statement QUERY = connection.createStatement();
        return QUERY.executeQuery(statement);
    }

}
