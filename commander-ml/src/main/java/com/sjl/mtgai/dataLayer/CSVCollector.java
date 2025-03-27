package com.sjl.mtgai.dataLayer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

public class CSVCollector extends DataCollector{

    private String CSVFolder;

    public CSVCollector(String CSVFolder) {
        this.CSVFolder = CSVFolder;

        this.cards = new ArrayList<Card>();
        this.cardIDs = new HashMap<Integer, Card>();
        this.decks = new ArrayList<Deck>();
        this.deckIDs = new HashMap<Integer, Deck>();
        this.tournaments = new ArrayList<Tournament>();
        this.tournamentIDs = new HashMap<Integer, Tournament>();
    }

    @Override
    public void buildCards() {
        try {
            buildCSVCards();
        } catch (IOException e) {
            System.out.println("File " + CSVFolder + "/refined_cards.csv not found!");
            e.printStackTrace();
        }
    }

    @Override
    public void buildDecks() {
        try {
            buildCSVDecks();
        } catch (IOException   e) {
            System.out.println("File " + CSVFolder + "/refined_decks.csv not found!");
            e.printStackTrace();
        }
    }

    @Override
    public void buildTournaments() {
        try {
            buildCSVTournaments();
        } catch (IOException  e) {
            System.out.println("File " + CSVFolder + "/normalized_tournament.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVCards() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/refined_cards.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true) // Skips the first row
            .setIgnoreSurroundingSpaces(true) // Removes extra spaces
            .setIgnoreEmptyLines(true) // Ignores blank lines
            .setQuote('"') // Ensures quoted values work properly
            .get()
            .parse(reader); 

        for (CSVRecord record : parser) {
            if (record.size() != 13) {  
                System.err.println("Malformed row detected: " + record);
                continue;  // Skip malformed rows
            }

            Card newCard = new Card(
                Integer.parseInt(record.get("id")),  // ID (not null)
                record.get("name"),  // Name (not null)
                record.get("facename").isEmpty() ? null : record.get(3),  // Face name (nullable)
                record.get("full_type"),  // Full type (not null)
                convertKeywords(record.get("keywords")),  // Keywords
                convertColor(record.get("coloridentity")),  // Color identity
                record.get("manavalue").isEmpty() ? Double.NaN : Double.parseDouble(record.get("manavalue")), // Mana value
                convertMana(record.get("manacost")),  // Mana cost
                record.get("power").isEmpty() ? null : record.get("power"), // Power (nullable)
                record.get("toughness").isEmpty() ? null : record.get("toughness"), // Toughness (nullable)
                Boolean.parseBoolean(record.get("gamechanger")),  // Game changer (not null)
                record.get("text").isEmpty() ? null : record.get("text"), // Card text
                null  // Linked card (optional, processed later)
            );

            cards.add(newCard);
            cardIDs.put(newCard.getId(), newCard);
        }
        reader.close();
        parser.close();
        linkCards();
    }

    // private static String[] fixMalformedRow(String[] tokens) {
    //     List<String> fixedTokens = new ArrayList<>();
    //     StringBuilder mergedField = new StringBuilder();

    //     boolean insideQuotes = false;
    //     for (String token : tokens) {
    //         if (token.startsWith("\"") && !insideQuotes) {
    //             insideQuotes = true;
    //             mergedField = new StringBuilder(token);
    //         } else if (token.endsWith("\"") && insideQuotes) {
    //             insideQuotes = false;
    //             mergedField.append(",").append(token);
    //             fixedTokens.add(mergedField.toString());
    //         } else if (insideQuotes) {
    //             mergedField.append(",").append(token);
    //         } else {
    //             fixedTokens.add(token);
    //         }
    //     }

    //     return fixedTokens.toArray(new String[0]);
    // }

    private void buildCSVDecks() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/refined_decks.csv");
        CSVParser csvParser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true) // Skips the first row
            .setIgnoreSurroundingSpaces(true) // Removes extra spaces
            .setIgnoreEmptyLines(true) // Ignores blank lines
            .setQuote('"') // Ensures quoted values work properly
            .get()
            .parse(reader); 

        for (CSVRecord record : csvParser) {
            // Ensure we don't process an incomplete row
            if (record.size() < 6) { 
                System.err.println("Skipping malformed row: " + record);
                continue;
            }

            Deck newDeck = new Deck(
                Integer.parseInt(record.get("id")),  // ID
                Integer.parseInt(record.get("tournament")),  // Tournament ID
                convertCommander(record.get("commander"), record.get("partner")),  // Commander & Partner
                null,  // Placeholder for tournament reference
                new ArrayList<>(),  // Empty decklist (filled later)
                false,  // Default is not padded
                0.0  // Default rank percentage
            );

            decks.add(newDeck);
            deckIDs.put(newDeck.getId(), newDeck);
        }
        buildDeckCards(); // Calls next step
    }

    @Override
    protected void buildDeckCards() {
        try {
            buildCSVDeckCards();
        } catch (IOException  e) {
            System.out.println("File " + CSVFolder + "/deck_cards.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVDeckCards() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/deck_cards.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true) // Skips the first row
            .setIgnoreSurroundingSpaces(true) // Removes extra spaces
            .setIgnoreEmptyLines(true) // Ignores blank lines
            .setQuote('"') // Ensures quoted values work properly
            .get()
            .parse(reader);
    
        for (CSVRecord record : parser) {
            int deckId = Integer.parseInt(record.get("deck_id"));
            int cardId = Integer.parseInt(record.get("card_id"));

            if (deckIDs.containsKey(deckId) && cardIDs.containsKey(cardId)) {
                deckIDs.get(deckId).addCard(cardIDs.get(cardId));
            } else {
                System.err.println("Warning: Deck ID or Card ID not found - Deck: " + deckId + ", Card: " + cardId);
            }
        }
    }

    private void buildCSVTournaments() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/normalized_tournament.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true) // Skips the first row
            .setIgnoreSurroundingSpaces(true) // Removes extra spaces
            .setIgnoreEmptyLines(true) // Ignores blank lines
            .setQuote('"') // Ensures quoted values work properly
            .get()
            .parse(reader);
    
        for (CSVRecord record : parser) {
            Tournament newTournament = new Tournament(
                Integer.parseInt(record.get("id"))
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
            buildCSVTournamentEntries();
        } catch (IOException e) {
            System.out.println("File " + CSVFolder + "/refined_decks.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVTournamentEntries() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/refined_decks.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true) // Skips the first row
            .setIgnoreSurroundingSpaces(true) // Removes extra spaces
            .setIgnoreEmptyLines(true) // Ignores blank lines
            .setQuote('"') // Ensures quoted values work properly
            .get()
            .parse(reader);
    
        for (CSVRecord record : parser) {
            Deck deck = deckIDs.get(Integer.parseInt(record.get("id")));
            Tournament tournament = tournamentIDs.get(Integer.parseInt(record.get("tournament")));

            tournament.addEntry(deck, record.get("rank"));
            deck.setTournament(tournament);
        }
    }
}
