package com.sjl.mtgai.dataLayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataCollector {

    private DataBaseConnector connection;

    private ArrayList<Card> cards;
    private Map<Integer, Card> cardIDs;
    private ArrayList<Deck> decks;
    private Map<Integer, Deck> deckIDs;
    private ArrayList<Tournament> tournaments;
    private Map<Integer, Tournament> tournamentIDs;

    public DataCollector(DataBaseConnector connection) {
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
    public void buildCards() throws SQLException {
                
        ResultSet set = connection.select("*", "refined_cards");
        while (set.next()) {
            Card newcard = new Card(
                set.getInt("id"),
                set.getString("name"),
                set.getString("facename"),
                set.getString("full_type"),
                set.getString("coloridentity"),
                set.getInt("manavalue"),
                set.getString("manacost").toCharArray(),
                set.getString("power"),
                set.getString("toughness"),
                set.getString("text"),
                null
            );
            cards.add(newcard);
            cardIDs.put(newcard.getId(), newcard);
        };

        linkCards();
    }

    /*
     * Helper method to collect deck data from the database and build the array list of decks.
     */
    public void buildDecks() throws SQLException {

        ResultSet set = connection.select("*", "refined_decks");
        while (set.next()) {
            Deck newdeck = new Deck( 
                set.getInt("id"),
                set.getString("commander"),
                set.getString("partner"),
                new ArrayList<Card>()
            );
            decks.add(newdeck);
            deckIDs.put(newdeck.getId(), newdeck);
        }

        buildDeckCards();
    }

    /*
     * Helper method to collect tournament data from the database.
     */
    public void buildTournaments() throws SQLException {

        ResultSet set = connection.select("DISTINCT tournament", "normalized_tournament");
        while (set.next()) {
            Tournament newTournament = new Tournament(
                set.getInt("tournament")
                );
            tournaments.add(newTournament);
            tournamentIDs.put(newTournament.getId(), newTournament);            
        }

        buildTournamentEntries();
    }

    private void buildDeckCards() throws SQLException {
        
        for (Deck deck : decks) {
            ResultSet set = connection.query("SELECT card_id FROM deck_cards WHERE deck_id = " + deck.getId());
            while (set.next()) {
                deck.addCard(cardIDs.get(set.getInt("card_id")));
            }
        }
    }

    private void buildTournamentEntries() throws SQLException {

        for (Tournament tournament : tournaments) {
            ResultSet set = connection.query("SELECT id, rank FROM refined_decks WHERE tournament = " + tournament.getId());
            while (set.next()) {
                tournament.addEntry(deckIDs.get(set.getInt("id")), set.getString("rank"));
            }
        }

    }

    private void linkCards() {
        Map<String, List<Card>> doubleCard = new HashMap<String, List<Card>>();
        for (Card card : cards) {
            if (card.getFacename() != null) {
                doubleCard.computeIfAbsent(card.getName(), k -> new ArrayList<>()).add(card);
            }
        }
        for (List<Card> linkedCards : doubleCard.values()) {
            linkedCards.get(0).linkCard(linkedCards.get(1));
        }

    }
}
