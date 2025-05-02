package com.sjl.mtgai.dataLayer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import com.sjl.mtgai.api.ScryfallAPI;
import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

public class CSVCollector extends DataCollector {

    private String CSVFolder;
    private ScryfallAPI api; // Add the API as a helper

    public CSVCollector(String CSVFolder, ScryfallAPI api) {
        this.CSVFolder = CSVFolder;
        this.api = api;

        this.cards = new ArrayList<>();
        this.cardIDs = new HashMap<>();
        this.decks = new ArrayList<>();
        this.deckIDs = new HashMap<>();
        this.tournaments = new ArrayList<>();
        this.tournamentIDs = new HashMap<>();
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

    private void buildCSVCards() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/refined_cards.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .setQuote('"')
            .get()
            .parse(reader);

        for (CSVRecord record : parser) {
            try {
                Card newCard = createCardFromCSV(record);
                cards.add(newCard);
                cardIDs.put(newCard.getId(), newCard);
            } catch (Exception e) {
                System.err.println("Malformed row detected in CSV: " + record);
                System.err.println("Attempting to fetch card data from API...");
                fetchCardFromAPI(record.get("name"));
            }
        }

        reader.close();
        parser.close();
        linkCards();
    }

    private Card createCardFromCSV(CSVRecord record) {
        return new Card(
            Integer.parseInt(record.get("id")),
            record.get("name"),
            record.get("facename").isEmpty() ? null : record.get("facename"),
            record.get("full_type"),
            convertKeywords(record.get("keywords")),
            convertColor(record.get("coloridentity")),
            record.get("manavalue").isEmpty() ? Double.NaN : Double.parseDouble(record.get("manavalue")),
            convertMana(record.get("manacost")),
            record.get("power").isEmpty() ? null : record.get("power"),
            record.get("toughness").isEmpty() ? null : record.get("toughness"),
            Boolean.parseBoolean(record.get("gamechanger")),
            record.get("text").isEmpty() ? null : record.get("text"),
            null
        );
    }

    private void fetchCardFromAPI(String cardName) {
        try {
            JSONObject jsonObject = api.getCardByFuzzyName(cardName);
            Map<String, Object> cardData = jsonObject.toMap();
            if (cardData != null) {
                Card newCard = createCardFromAPIData(cardData);
                cards.add(newCard);
                cardIDs.put(newCard.getId(), newCard);
            } else {
                System.err.println("Card not found in API: " + cardName);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch card data from API for: " + cardName);
            e.printStackTrace();
        }
    }

    private Card createCardFromAPIData(Map<String, Object> cardData) {
        return new Card(
            (int) cardData.get("id"),
            (String) cardData.get("name"),
            (String) cardData.get("facename"),
            (String) cardData.get("type"),
            convertKeywords((String) cardData.get("keywords")),
            convertColor((String) cardData.get("colorIdentity")),
            (double) cardData.get("manaValue"),
            convertMana((String) cardData.get("manaCost")),
            (String) cardData.get("power"),
            (String) cardData.get("toughness"),
            (Boolean) cardData.get("gamechanger"),
            (String) cardData.get("text"),
            null
        );
    }

    @Override
    public void buildDecks() {
        try {
            buildCSVDecks();
        } catch (IOException e) {
            System.out.println("File " + CSVFolder + "/refined_decks.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVDecks() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/refined_decks.csv");
        CSVParser csvParser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .setQuote('"')
            .get()
            .parse(reader);

        for (CSVRecord record : csvParser) {
            if (record.size() < 6) {
                System.err.println("Skipping malformed row: " + record);
                continue;
            }

            Deck newDeck = new Deck(
                Integer.parseInt(record.get("id")),
                Integer.parseInt(record.get("tournament")),
                convertCommander(record.get("commander"), record.get("partner")),
                null,
                new ArrayList<>(),
                false,
                0.0
            );

            decks.add(newDeck);
            deckIDs.put(newDeck.getId(), newDeck);
        }
        buildDeckCards();
    }

    @Override
    protected void buildDeckCards() {
        try {
            buildCSVDeckCards();
        } catch (IOException e) {
            System.out.println("File " + CSVFolder + "/deck_cards.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVDeckCards() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/deck_cards.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .setQuote('"')
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

    @Override
    public void buildTournaments() {
        try {
            buildCSVTournaments();
        } catch (IOException e) {
            System.out.println("File " + CSVFolder + "/normalized_tournament.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVTournaments() throws IOException {
        Reader reader = new FileReader(CSVFolder + "/normalized_tournament.csv");
        CSVParser parser = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .setQuote('"')
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
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .setQuote('"')
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
