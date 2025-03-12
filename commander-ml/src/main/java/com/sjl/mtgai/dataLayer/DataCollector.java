package com.sjl.mtgai.dataLayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public DataCollector(DataBaseConnector connection) {
        this.connection = connection;

        this.cards = new ArrayList<Card>();
        this.cardIDs = new HashMap<Integer, Card>();
        this.decks = new ArrayList<Deck>();
        this.deckIDs = new HashMap<Integer, Deck>();
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
                set.getInt("manavalue"),
                convertMana(set.getString("manacost")),
                set.getString("power"),
                set.getString("toughness"),
                set.getBoolean("gamechanger"),
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
                convertRank(set.getString("rank")),
                new ArrayList<Card>()
            );
            decks.add(newdeck);
            deckIDs.put(newdeck.getId(), newdeck);
        }

        buildDeckCards();
    }

    private void buildDeckCards() throws SQLException {
        
        for (Deck deck : decks) {
            ResultSet set = connection.query("SELECT card_id FROM deck_cards WHERE deck_id = " + deck.getId());
            while (set.next()) {
                deck.addCard(cardIDs.get(set.getInt("card_id")));
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
            if (linkedCards.size() >= 2)
                linkedCards.get(0).linkCard(linkedCards.get(1));
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

    private char[] convertColor(String colorIdentity) {
        if(colorIdentity != null)
            return colorIdentity.replaceAll(",", " ").toCharArray();
        else
            return new char[0];
    }

    private double convertRank(String rank) {
        Pattern pattern = Pattern.compile("(\\d+)%");
        Matcher matcher = pattern.matcher(rank);
        if (matcher.find()) {
            String numberStr = matcher.group(1);
            double percentage = Double.parseDouble(numberStr) / 100.0; // Converts "50" to 0.5
            return percentage;
        } else {
            return Double.NaN;
        }
    }

    private String[] convertKeywords(String keyString) {
        if (keyString == null)
            return new String[0];
        else {
            return keyString.split(",");
        }
    }
}
