package com.sjl.mtgai.dataLayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;

public class CSVReader {

    public static HashMap<String, HashSet<String>> comboMap = new HashMap<String, HashSet<String>>();

    public static void buildComboMap() {
        String filePath = "database_files/infinite_combos.csv";  // Path to the CSV file

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Loop through each line in the CSV file
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");  // Split the line by commas

                String keyCard = tokens[0].trim();  // The first card is the key card
                
                // Add the combo cards for this key card
                for (int i = 1; i < tokens.length; i++) {
                    String comboCard = tokens[i].trim();  // Combo card is at index i
                    
                    // If the keyCard isn't already in the map, initialize the set for it
                    comboMap.putIfAbsent(keyCard, new HashSet<>());
                    
                    // Add the combo card to the set of combos for the key card
                    comboMap.get(keyCard).add(comboCard);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Tournament> getUserTournaments(String fileLocation, DataCollector collector) {
        HashMap<Integer, Tournament> tournaments = new HashMap<Integer,Tournament>();
        HashMap<Integer, Deck> tournamentDecks = new HashMap<Integer, Deck>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
            String line; //tournament_id, deck_id, commander, partner, author, card

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("tournament_id")) {
                    continue; // Skip the header line
                }
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                int tournamentID = Integer.parseInt(tokens[0].replaceAll("^\"|\"$", "").trim());
                int deckID = Integer.parseInt(tokens[1].replaceAll("^\"|\"$", "").trim());
                String commander = tokens[2].replaceAll("^\"|\"$", "").trim();
                String partner = tokens[3].replaceAll("^\"|\"$", "").trim();
                // String author = tokens[4].replaceAll("^\"|\"$", "").trim();
                String cardName = tokens[5].replaceAll("^\"|\"$", "").trim();

                if (!tournaments.containsKey(tournamentID)) {
                    tournaments.put(tournamentID, new Tournament(tournamentID));
                }

                if (!tournamentDecks.containsKey(deckID)) {
                    tournamentDecks.put(deckID, new Deck(deckID, tournamentID, getCommanderCards(commander, partner, collector)));
                } else {
                    tournamentDecks.get(deckID).addCard(getCard(cardName, collector));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Deck deck : tournamentDecks.values()) {
            tournaments.get(deck.getTournamentID()).addEntry(deck, null);
            deck.padDeck();
        }

        return new ArrayList<>(tournaments.values().stream().toList());
    }

    private static ArrayList<Card> getCommanderCards(String commander, String partner, DataCollector collector) {
        ArrayList<Card> commanderCards = new ArrayList<Card>();
        commanderCards.add(getCard(commander, collector));
        if (!partner.equals("N/A")) {
            commanderCards.add(getCard(partner, collector));
        }
        return commanderCards;
    }
    
    private static Card getCard(String cardName, DataCollector collector) {
        for (Card card : collector.getCards()) {
            String name = card.getName();
            String faceName = card.getFacename();
            if (faceName != null && faceName.equals(normalizeString(cardName))) {
                return card;
            }
            if(faceName == null && name.equals(normalizeString(cardName)))
                return card;
        }
        System.out.println("Card not found: " + cardName);
        return null;
    }

    // Normalize string by removing accents and decomposing special characters
    private static String normalizeString(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFKD)
                        .replaceAll("\\p{M}", "");
    }
}
