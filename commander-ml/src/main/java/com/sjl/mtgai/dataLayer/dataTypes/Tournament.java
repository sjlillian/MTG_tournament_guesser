package com.sjl.mtgai.dataLayer.dataTypes;

import java.util.ArrayList;

/**
 * A class the represents a tournament. It contains a list of entries (see TournamentEntry.java) and the tournament id. A tournament can
 * have as little as 4 decks.
 */

public class Tournament {
    
    private int id;
    private ArrayList<TournamentEntry> entries;

    /**
     * Constructor that creates an array list for tournament entries.
     */
    public Tournament(int id) {
        this.id = id;
        this.entries = new ArrayList<TournamentEntry>();
    }

    public Tournament() {
        this.id = -1;
        this.entries = new ArrayList<TournamentEntry>();
    }

    public void addEntry(Deck deck, String rank) {
        entries.add(new TournamentEntry(deck, rank));
    }

    public ArrayList<Deck> getDecks() {
        ArrayList<Deck> decks = new ArrayList<Deck>();
        for (TournamentEntry entry : entries) {
            decks.add(entry.getDeck());
        }
        return decks;
    }

    public ArrayList<TournamentEntry> getEntries() {
        return entries;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return entries.size();
    }

}
