package com.sjl.mtgai;

import java.sql.SQLException;

import com.sjl.mtgai.dataLayer.DataBaseConnector;
import com.sjl.mtgai.dataLayer.DataCollector;

public class DataLayerController {

    private static DataCollector collector;

    public static void DataCollector() throws SQLException {
         DataBaseConnector connection = new DataBaseConnector();

        try {
            connection.getConnection();
            System.out.println("Connection Successful!");
        } catch (SQLException e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }

        collector = new DataCollector(connection);
        collector.buildCards();
        collector.buildDecks();
        collector.buildTournaments();

        System.out.println(collector.getCards().size());
        System.out.println(collector.getDecks().size());


        try {
            connection.closeConnection();
            System.out.println("Connection Closed!");
        } catch (SQLException e) {
            System.out.println("ERROR CLOSING CONNECTION!");
            e.printStackTrace();
        }
    }

    public static DataCollector getCollector() {
        return collector;
    }

}
