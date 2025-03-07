package com.sjl.mtgai.dataLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A class the represents a tournament. It contains a list of entries (see TournamentEntry.java) and the tournament id. A tournament can
 * have as little as 4 decks.
 */

public class Tournament {
    
    private int id;
    private List<TournamentEntry> entries;

    /**
     * Constructor that creates an array list for tournament entries.
     */
    public Tournament(int id) {
        this.id = id;
        this.entries = new ArrayList<TournamentEntry>();
    }

    public void addEntry(Deck deck, String rank) {
        entries.add(new TournamentEntry(deck, rank));
    }

    public List<TournamentEntry> getEntries() {
        return entries;
    }

    public int getId() {
        return id;
    }

}
