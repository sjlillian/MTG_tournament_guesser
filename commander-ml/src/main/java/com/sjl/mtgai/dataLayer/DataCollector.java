package com.sjl.mtgai.dataLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DataCollector {

    protected ArrayList<Card> cards;
    protected Map<Integer, Card> cardIDs;
    protected ArrayList<Deck> decks;
    protected Map<Integer, Deck> deckIDs;
    protected ArrayList<Tournament> tournaments;
    protected Map<Integer, Tournament> tournamentIDs;

    public abstract void buildCards();
    public abstract void buildDecks();
    protected abstract void buildDeckCards();
    public abstract void buildTournaments();
    protected abstract void buildTournamentEntries();

    protected void linkCards() {
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

    protected char[] convertColor(String colorIdentity) {
        if(colorIdentity != null)
            return colorIdentity.replaceAll(",", " ").toCharArray();
        else
            return new char[0];
    }

    protected ArrayList<String> convertKeywords(String keyString) {
        if (keyString == null)
            return new ArrayList<String>();
        else {
            return new ArrayList<String>(Arrays.asList(keyString.split(",")));
        }
    }

    protected ArrayList<Character> convertMana(String manacost) {
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

    protected ArrayList<Card> convertCommander(String commander, String partner) {
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
}
