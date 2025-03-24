package com.sjl.mtgai.dataLayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

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
            // System.out.println("Card: " + newcard.getName() + " added to the database.");
        };

        linkCards();
    }

    private void linkCards() {
        Map<String, List<Card>> doubleCard = new HashMap<String, List<Card>>();
        for (Card card : cards) {
            if (card.getFacename() != null) {
                doubleCard.computeIfAbsent(card.getName(), k -> new ArrayList<>()).add(card);
            }
        }
        for (List<Card> linkedCards : doubleCard.values()) {
            if (linkedCards.size() >= 2)
                linkedCards.get(0).linkCard(linkedCards.get(1));
        }

    }

    private char[] convertColor(String colorIdentity) {
        if(colorIdentity != null)
            return colorIdentity.replaceAll(",", " ").toCharArray();
        else
            return new char[0];
    }

    private ArrayList<String> convertKeywords(String keyString) {
        if (keyString == null)
            return new ArrayList<String>();
        else {
            return new ArrayList<String>(Arrays.asList(keyString.split(",")));
        }
    }

    private ArrayList<Character> convertMana(String manacost) {
        if (manacost == null) {
            ArrayList<Character> zero = new ArrayList<Character>();
            zero.add('-');
            return zero;
        } else {
            char[] manacostArray = manacost.toCharArray();
            ArrayList<Character> manaList = new ArrayList<Character>();
            for (char c : manacostArray) {
                manaList.add(c);            
            }
            ArrayList<Character> removeCharacters = new ArrayList<Character>();
            removeCharacters.add(Character.valueOf('{'));
            removeCharacters.add(Character.valueOf('}'));
            manaList.removeAll(removeCharacters);
            return manaList;
        }
    }

    /*
     * Helper method to collect deck data from the database and build the array list of decks.
     */
    public void buildDecks() throws SQLException {

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

    private ArrayList<Card> convertCommander(String commander, String partner) {
        ArrayList<Card> commanderList = new ArrayList<Card>();
        for (Card card : cards) {
            if(card.getName().equals(commander)) 
                commanderList.add(card);
        }

        if (partner != null) {
            for (Card card : cards) {
                if(card.getName().equals(partner)) 
                    commanderList.add(card);
            }
        }

        return commanderList;
    }

    private void buildDeckCards() throws SQLException {
        
        for (Deck deck : decks) {
            ResultSet set = connection.query("SELECT card_id FROM deck_cards WHERE deck_id = " + deck.getId());
            while (set.next()) {
                deck.addCard(cardIDs.get(set.getInt("card_id")));
            }
        }
    }

    public void buildTournaments() throws SQLException {

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

    private void buildTournamentEntries() throws SQLException {

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
