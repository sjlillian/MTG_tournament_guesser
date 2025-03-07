package com.sjl.mtgai;

import java.sql.SQLException;

import com.sjl.mtgai.dataLayer.DataBaseConnector;
import com.sjl.mtgai.dataLayer.DataCollector;

/**
 * Hello world!
 *
 */
public class App {
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

        DataCollector collector = new DataCollector(connection);
        collector.buildCards();
        collector.buildDecks();
        collector.buildTournaments();

        System.out.println(collector.getCards().size());
        System.out.println(collector.getDecks().size());
        System.out.println(collector.getDeckIDs().get(1).getDeckList().size());
        System.out.println(collector.getTournaments().size());
        System.out.println(collector.getTournamentIDs().get(1).getEntries().size());
        


        try {
            connection.closeConnection();
            System.out.println("Connection Closed!");
        } catch (SQLException e) {
            System.out.println("ERROR CLOSING CONNECTION!");
            e.printStackTrace();
        }

    }
}
