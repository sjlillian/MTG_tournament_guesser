package com.sjl.mtgai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sjl.mtgai.dataLayer.Card;
import com.sjl.mtgai.dataLayer.DataBaseConnector;
import com.sjl.mtgai.dataLayer.Deck;
import com.sjl.mtgai.dataLayer.Tournament;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws SQLException {
        System.out.println( "Hello World!" );
        DataBaseConnector connection = new DataBaseConnector();
        ArrayList<Card> cards;
        ArrayList<Deck> decks;
        ArrayList<Tournament> tournaments;

        try {
            connection.getConnection();
            System.out.println("Connection Successful!");
        } catch (SQLException e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }

        cards = getCards(connection);
        decks = getDecks(connection);
        tournaments = getTournaments(connection);

        
        System.out.println(cards.get(1));
        System.out.println(decks.get(1));
        System.out.println(tournaments.size());
        


        try {
            connection.closeConnection();
            System.out.println("Connection Closed!");
        } catch (SQLException e) {
            System.out.println("ERROR CLOSING CONNECTION!");
            e.printStackTrace();
        }

    }

    /*
     * Helper method to collect card data from the database.
     */
    private static ArrayList<Card> getCards(DataBaseConnector connection) throws SQLException {
        ArrayList<Card> list = new ArrayList<Card>();
        
        ResultSet set = connection.select("*", "refined_cards");
        while (set.next()) {
            list.add(new Card(
                set.getInt("id"),
                set.getString("name"),
                set.getString("facename"),
                set.getString("full_type"),
                set.getString("coloridentity"),
                set.getString("manacost"),
                set.getString("power"),
                set.getString("toughness"),
                set.getString("text"),
                null
            ));
        };
        return list;
    }

    /*
     * Helper method to collect deck data from the database.
     */
    private static ArrayList<Deck> getDecks(DataBaseConnector connection) throws SQLException {
        ArrayList<Deck> list = new ArrayList<Deck>();

        ResultSet set = connection.select("*", "refined_decks");
        while (set.next()) {
            list.add(new Deck( 
                set.getInt("id"),
                set.getString("commander"),
                set.getString("partner"),
                new ArrayList<Card>()
            ));
        }



        return list;
    }

    /*
     * Helper method to collect tournament data from the database.
     */
    private static ArrayList<Tournament> getTournaments(DataBaseConnector connection) throws SQLException {
        ArrayList<Tournament> list = new ArrayList<Tournament>();

        ResultSet set = connection.select("*", "refined_cards");
        while (set.next()) {
            list.add(new Tournament(
                set.getInt("id")
                ));
            
        }

        return list;
    }
}
