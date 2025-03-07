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
        //decks = getDecks(connection);
        //tournaments = getTournaments(connection);

        
        System.out.println(cards.size());
        //System.out.println(decks.size());
        //System.out.println(tournaments.size());
        


        try {
            connection.closeConnection();
            System.out.println("Connection Closed!");
        } catch (SQLException e) {
            System.out.println("ERROR CLOSING CONNECTION!");
            e.printStackTrace();
        }

    }

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

    private static ArrayList<Deck> getDecks(DataBaseConnector connection) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCards'");
    }

    private static ArrayList<Tournament> getTournaments(DataBaseConnector connection) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCards'");
    }
}
