package com.sjl.mtgai.dataLayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;


public class DBCollector extends DataCollector {

    private DataBaseConnector connection;

    public DBCollector(DataBaseConnector connection) {
        this.connection = connection;

        this.cards = new ArrayList<Card>();
        this.cardIDs = new HashMap<Integer, Card>();
        this.decks = new ArrayList<Deck>();
        this.deckIDs = new HashMap<Integer, Deck>();
        this.tournaments = new ArrayList<Tournament>();
        this.tournamentIDs = new HashMap<Integer, Tournament>();
    }


    /*
     * Helper method to collect card data from the database and build the array list of ALL cards.
     */
    @Override
    public void buildCards() {
        try {
            buildSQLCards();
        } catch (SQLException e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }
    }

    @Override
    public void buildDecks() {
        try {
            buildSQLDecks();
        } catch (Exception e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }
    }

    @Override
    public void buildTournaments() {
        try {
            buildSQLTournaments();
        } catch (Exception e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }
    }
    
    
    private void buildSQLCards() throws SQLException {
                
        ResultSet set = connection.select("*", "refined_cards");
        while (set.next()) {
            Card newcard = new Card(
                set.getInt("id"),
                set.getString("name"),
                set.getString("facename"),
                set.getString("full_type"),
                convertKeywords(set.getString("keywords")),
                convertColor(set.getString("coloridentity")),
                set.getDouble("manavalue"),
                convertMana(set.getString("manacost")),
                set.getString("power"),
                set.getString("toughness"),
                set.getBoolean("gamechanger"),
                set.getString("text"),
                null
            );
            cards.add(newcard);
            cardIDs.put(newcard.getId(), newcard);
        }

        linkCards();
    }

    /*
     * Helper method to collect deck data from the database and build the array list of decks.
     */
    private void buildSQLDecks() throws SQLException {

        ResultSet set = connection.select("*", "refined_decks");
        while (set.next()) {
            Deck newdeck = new Deck( 
                set.getInt("id"),
                set.getInt("tournament"),
                convertCommander(set.getString("commander"), set.getString("partner")),
                null,
                new ArrayList<Card>(),
                false,
                0.0
            );
            decks.add(newdeck);
            deckIDs.put(newdeck.getId(), newdeck);
        }

        buildDeckCards();
        for (Deck deck : decks) {
            deck.padDeck();
        }
    }

    @Override
    protected void buildDeckCards() {
        try {
            buildSQLDeckCards();
        } catch (Exception e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }
    }

    private void buildSQLDeckCards() throws SQLException {
        
        for (Deck deck : decks) {
            ResultSet set = connection.query("SELECT card_id FROM deck_cards WHERE deck_id = " + deck.getId());
            while (set.next()) {
                deck.addCard(cardIDs.get(set.getInt("card_id")));
            }
        }
    }

    private void buildSQLTournaments() throws SQLException {

        ResultSet set = connection.select("DISTINCT tournament", "normalized_tournament");
        Tournament newTournament = new Tournament();
        while (set.next()) {
            newTournament = new Tournament(
                set.getInt("tournament")
                );
            tournaments.add(newTournament);
            tournamentIDs.put(newTournament.getId(), newTournament);            
        }

        buildTournamentEntries();
        for (Tournament tournament : tournaments) {
            for (TournamentEntry entry : tournament.getEntries()) {
                entry.convertRank(tournament.getEntries().size());
            }
        }
    }

    @Override
    protected void buildTournamentEntries() {
        try {
            buildSQLTournamentEntries();
        } catch (Exception e) {
            System.out.println("Connection Failed:");
            e.printStackTrace();
        }
    }

    private void buildSQLTournamentEntries() throws SQLException {

        for (Tournament tournament : tournaments) {
            ResultSet set = connection.query("SELECT id, rank FROM refined_decks WHERE tournament = " + tournament.getId());
            while (set.next()) {
                Deck deck = deckIDs.get(set.getInt("id"));
                tournament.addEntry(
                    deck,
                    set.getString("rank")
                    );
                deck.setTournament(tournament);
            }
        }

    }
    
}
