package com.sjl.mtgai;

import java.sql.SQLException;

import com.sjl.mtgai.dataLayer.CSVCollector;
import com.sjl.mtgai.dataLayer.CSVReader;
import com.sjl.mtgai.dataLayer.DBCollector;
import com.sjl.mtgai.dataLayer.DataBaseConnector;
import com.sjl.mtgai.dataLayer.DataCollector;

public class DataLayerController {

    private static DataCollector collector;

    public static void CSVCollector() {
        CSVReader.buildComboMap();

        collector = new CSVCollector("database_files");

        
        collector.buildCards();
        collector.buildDecks();
        collector.buildTournaments();

    }

    public static void DBCollector(String PDURL, String username, String password) throws SQLException {
        CSVReader.buildComboMap();
        DataBaseConnector connection = new DataBaseConnector(PDURL, username, password);

        try {
            connection.getConnection();
            System.out.println("Connection Successful!");
        } catch (SQLException e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }

        collector = new DBCollector(connection);
        collector.buildCards();
        collector.buildDecks();
        collector.buildTournaments();

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
