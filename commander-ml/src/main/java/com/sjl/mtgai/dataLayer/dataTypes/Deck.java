package com.sjl.mtgai.dataLayer.dataTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A class that represents a deck of cards for the commander format. In a commander deck, there is a total of 100 cards with a sideboard of
 * 15 extra cards. Of these 100 cards, one (or two) is designated the commander. There can be no duplicate cards except for basic lands.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Deck {

    private int id;
    private int tournamentID;
    private ArrayList<Card> commander;
    private Tournament tournament;
    private ArrayList<Card> deckList;
    private Boolean isPadded;
    private double paddedRatio;

    public Deck(int id, int tournamentID, ArrayList<Card> commander) {
        this.id = id;
        this.tournamentID = tournamentID;
        this.tournament = new Tournament();
        this.commander = commander;
        this.deckList = new ArrayList<Card>();
        this.isPadded = false;
    }

    public void addCard(Card card) {
        deckList.add(card);
    }

    public double getRankPercentage() {
        for (TournamentEntry entry : tournament.getEntries()) {
            if (entry.getDeck() == this)
                return entry.getRankPercentage();
        }
        return Double.NaN;
    }

    public int getRankBracket() {
        for (TournamentEntry entry : tournament.getEntries()) {
            if (entry.getDeck() == this) 
                return entry.getRankBracket();
        }
        return 0;
    }

    public String toString() {
        return new String("Tournamnet ID: " + this.tournamentID + "\nDeck ID: "+ this.id + "\nCommander: " + this.getCommanderNames());
    }

    public void padDeck() {
        if (this.deckList.size() <= 100){
            this.isPadded = true;
            int insertedCards = 100 - deckList.size();
            this.paddedRatio = insertedCards / 100;
            while (insertedCards > 0) {
                this.addCard(new Card(0, "Basic Land Padder", null, "Basic Land", new ArrayList<String>(), new char[0], 0, new ArrayList<Character>(), "0", "0", false, "{T} for mana", null));
                insertedCards--;
            }
        }
    }

    public String getCommanderNames() {
        List<String> names = this.getCommander().stream()
                         .map(Card::getName)
                         .collect(Collectors.toList());
        Collections.sort(names);
        return String.join(" & ", names);
    }

}