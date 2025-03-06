package com.sjl.mtgai;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.sjl.mtgai.dataLayer.DataBaseConnector;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException {
        System.out.println( "Hello World!" );

        DataBaseConnector connection = new DataBaseConnector();

        try {
            connection.getConnection();
            System.out.println("Connection Successful!");
        } catch (SQLException e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }

        ResultSet set = connection.select("*", "refined_cards");

        while (set.next()) {
            System.out.println(set.getString("id") + set.getString("name") + set.getString("facename"));
        }

        


        try {
            connection.closeConnection();
            System.out.println("Connection Closed!");
        } catch (SQLException e) {
            System.out.println("ERROR CLOSING CONNECTION!");
            e.printStackTrace();
        }

    }
}
