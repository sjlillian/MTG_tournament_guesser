package com.sjl.mtgai.dataLayer;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
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
        } catch (IOException | CsvValidationException e) {
            System.out.println("File " + CSVFolder + "/refined_cards.csv not found!");
            e.printStackTrace();
        }
    }

    @Override
    public void buildDecks() {
        try {
            buildCSVDecks();
        } catch (IOException | CsvValidationException e) {
            System.out.println("File " + CSVFolder + "/refined_decks.csv not found!");
            e.printStackTrace();
        }
    }

    @Override
    public void buildTournaments() {
        try {
            buildCSVTournaments();
        } catch (IOException | CsvValidationException e) {
            System.out.println("File " + CSVFolder + "/normalized_tournament.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVCards() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReaderBuilder(new FileReader(CSVFolder + "/refined_cards.csv"))
            .withCSVParser(new CSVParserBuilder()
                .withSeparator(',')  // Ensure it still uses commas
                .withQuoteChar('"')  // Ensure quotes are handled
                .withEscapeChar('\\') // Escape quotes properly
                .build())
            .build();
        String[] tokens; // id, gamechanger, layout, facename, keywords, manacost, coloridentity, manavalue, name, full_type, power, toughness, text
    
        while ((tokens = reader.readNext()) != null) {
            if (tokens.length != 13) {  
                System.err.println("Malformed row detected: " + Arrays.toString(tokens) + "\n");
                tokens = fixMalformedRow(tokens); // Auto-fix function
            }
            if (tokens[0].equals("id")) {
                continue; // Skip the header line
            }

            Card newcard = new Card(
                Integer.parseInt(tokens[0]), //not null
                tokens[8], //not null
                ((!tokens[3].equals("")) ? tokens[3] : null), //nullable
                tokens[9], //not null
                convertKeywords(tokens[4]),
                convertColor(tokens[6]),
                ((!tokens[7].equals("")) ? Double.parseDouble(tokens[7]) : Double.NaN),
                convertMana(tokens[5]),
                ((!tokens[10].equals("")) ? tokens[10] : null), //nullable
                ((!tokens[11].equals("")) ? tokens[11] : null), //nullable
                Boolean.parseBoolean(tokens[1]), //not null
                ((!tokens[12].equals("")) ? tokens[12] : null),
                null
            );
            cards.add(newcard);
            cardIDs.put(newcard.getId(), newcard);
        }
        reader.close();
        linkCards();
    }

    private static String[] fixMalformedRow(String[] tokens) {
        List<String> fixedTokens = new ArrayList<>();
        StringBuilder mergedField = new StringBuilder();

        boolean insideQuotes = false;
        for (String token : tokens) {
            if (token.startsWith("\"") && !insideQuotes) {
                insideQuotes = true;
                mergedField = new StringBuilder(token);
            } else if (token.endsWith("\"") && insideQuotes) {
                insideQuotes = false;
                mergedField.append(",").append(token);
                fixedTokens.add(mergedField.toString());
            } else if (insideQuotes) {
                mergedField.append(",").append(token);
            } else {
                fixedTokens.add(token);
            }
        }

        return fixedTokens.toArray(new String[0]);
    }

    private void buildCSVDecks() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(CSVFolder + "/refined_decks.csv"));
        String[] tokens; //id, commander, partner, tournament, author, rank
    
        while ((tokens = reader.readNext()) != null) {
            if (tokens[0].equals("id")) {
                continue; // Skip the header line
            }

            Deck newdeck = new Deck( 
                Integer.parseInt(tokens[0]),
                Integer.parseInt(tokens[3]),
                convertCommander(tokens[1], tokens[2]),
                null,
                new ArrayList<Card>(),
                false,
                0.0
            );
            decks.add(newdeck);
            deckIDs.put(newdeck.getId(), newdeck);
        }
        reader.close();
        buildDeckCards();
    }

    @Override
    protected void buildDeckCards() {
        try {
            buildCSVDeckCards();
        } catch (IOException | CsvValidationException e) {
            System.out.println("File " + CSVFolder + "/deck_cards.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVDeckCards() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(CSVFolder + "/deck_cards.csv"));
        String[] tokens; //rank, commander, partner, normalized_card, name, card_id, deck_id
    
        while ((tokens = reader.readNext()) != null) {
            if (tokens[0].equals("rank")) {
                continue; // Skip the header line
            }

            deckIDs.get(Integer.parseInt(tokens[6])).addCard(cardIDs.get(Integer.parseInt(tokens[5])));
        }
        reader.close();
    }

    private void buildCSVTournaments() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(CSVFolder + "/normalized_tournament.csv"));
        String[] tokens; //rank, commander, partner, normalized_card, author, tournament
    
        while ((tokens = reader.readNext()) != null) {
            if (tokens[0].equals("rank")) {
                continue; // Skip the header line
            }

            Tournament newTournament = new Tournament(
                Integer.parseInt(tokens[5])
            );
            tournaments.add(newTournament);
            tournamentIDs.put(newTournament.getId(), newTournament);            
        }
        reader.close();

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
        } catch (IOException | CsvValidationException e) {
            System.out.println("File " + CSVFolder + "/refined_decks.csv not found!");
            e.printStackTrace();
        }
    }

    private void buildCSVTournamentEntries() throws IOException, CsvValidationException {
        CSVReader reader = new CSVReader(new FileReader(CSVFolder + "/refined_decks.csv"));
        String[] tokens; //id, commander, partner, tournament, author, rank
    
        
        while ((tokens = reader.readNext()) != null) {
            if (tokens[0].equals("id")) {
                continue; // Skip the header line
            }
            Deck deck = deckIDs.get(Integer.parseInt(tokens[0]));
            Tournament tournament = tournamentIDs.get(Integer.parseInt(tokens[3]));
            tournament.addEntry(
                deck,
                tokens[5]
                );
            deck.setTournament(tournament);
        }
        reader.close();
    }
}
