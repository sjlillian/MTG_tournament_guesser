package com.sjl.mtgai.logicLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sjl.mtgai.dataLayer.Card;
import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.dataLayer.Deck;

import smile.data.DataFrame;

public class DataConverter {
    
    public static DataFrame deckDataFrame;

    public static void buildFrames(DataCollector collector) {
        ArrayList<Deck> decks = collector.getDecks();

        String[] columNames = {
            "ManaCostAvg", "TypeDiversity", "ColorDiversity", "Gamechangers", "KeywordSynergy", "WinLoss"
        };

        double[][] deckData = decks.stream()
        .map(deck -> new double[] {
            getManaCostAverage(deck),
            getTypeDiversity(deck),
            getColorDiversity(deck),
            getGamechangers(deck),
            getKeywordSynergy(deck),
            deck.getWinLoss()
        })
        .toArray(double[][]::new);

        deckDataFrame = DataFrame.of(deckData, columNames);

    }

    private static double getManaCostAverage(Deck deck) {
        double average = 0;
        int nonLand = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getManacost() != null) { 
                average += card.getManaValue();
                if (!card.getType().contains("Basic Land"))
                    nonLand ++;
            }
        }
        return average / nonLand;
    }

    private static double getTypeDiversity(Deck deck) {
        // Define the known card types (adjust these as necessary)
        String[] knownTypes = {
            "Artifact", "Creature", "Instant", "Sorcery", 
            "Enchantment", "Land", "Planeswalker", "Battlefield"
        };
        
        Map<String, Integer> typesPresent = new HashMap<>();
        for (String type : knownTypes) {
            typesPresent.put(type, 0);
        }

        for (Card card : deck.getDeckList()) {
            String cardTypeInfo = card.getType();
            for (String type : knownTypes) {
                if (cardTypeInfo.contains(type)) {
                    typesPresent.put(type, typesPresent.get(type) + 1);
                }
            }
        }
        
        
        int countMultiple = 0;
        for (Integer count : typesPresent.values()) {
            if (count > 1) {
                countMultiple++;
            }
        }
        
        return (double) countMultiple / knownTypes.length;
    }

    private static double getColorDiversity(Deck deck) {
        Set<Character> colorsPresent = new HashSet<>();
        for (Card card : deck.getDeckList()) {
            for (char color : card.getColors()) {
             colorsPresent.add(Character.valueOf(color));   
            }
        }
        return (colorsPresent.size() / 5.0);
    }

    private static double getGamechangers(Deck deck) {
        double amount = 0;
        for (Card card : deck.getDeckList()) {
            if (card.isGamechanger())
                amount ++;            
        }
        return amount;
    }

    private static double getKeywordSynergy(Deck deck) {
        Map<String, Integer> keywordFrequency = new HashMap<>();
        for (Card card : deck.getDeckList()) {
            for (String keyword : card.getKeywords()) {
                keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
            }
        }
        // For synergy, sum extra occurrences (if a keyword appears more than once)
        double synergy = 0;
        for (int count : keywordFrequency.values()) {
            if (count > 1) synergy += (count - 1);
        }
        // Normalize by number of cards (or total keywords)
        return synergy / deck.getDeckList().size();
    }

}
