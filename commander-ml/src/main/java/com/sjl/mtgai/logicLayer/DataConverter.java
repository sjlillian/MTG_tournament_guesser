package com.sjl.mtgai.logicLayer;

import java.util.ArrayList;

import com.sjl.mtgai.dataLayer.Card;
import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.dataLayer.Deck;

import smile.data.DataFrame;

public class DataConverter {
    
    public static DataFrame deckDataFrame;

    public static void buildFrames(DataCollector collector) {
        ArrayList<Deck> decks = collector.getDecks();

        String[] columNames = {
            "ManaCostAvg", "CreatureRatio", "SorceryRatio", "InstantRatio", "ArtifactRatio",
            "EnchantmentRatio", "LandRatio", "PlaneswalkerRatio", "WhiteRatio", "BlueRatio",
            "BlackRatio", "RedRatio", "GreenRatio", "ColorlessRatio", "WinLoss"
        };

        double[][] deckData = decks.stream()
        .map(deck -> new double[] {
            getManaCostAverage(deck),
            getTypeRatio(deck, "Creature"),
            getTypeRatio(deck, "Sorcery"),
            getTypeRatio(deck, "Instant"),
            getTypeRatio(deck, "Artifact"),
            getTypeRatio(deck, "Enchantment"),
            getTypeRatio(deck, "Land"),
            getTypeRatio(deck, "Planeswalker"),
            getColor(deck, 'W'),
            getColor(deck, 'U'),
            getColor(deck, 'B'),
            getColor(deck, 'R'),
            getColor(deck, 'G'),
            getColor(deck, ' '),
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

    private static double getTypeRatio(Deck deck, String type) {
        double total = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getType().contains(type))
                total ++;
        }
        return total / deck.getDeckList().size();
    }

    private static double getColor(Deck deck, Character color) {
        double colorMana = 0;
        double totalMana = 0;
        for (Card card : deck.getDeckList()) {
            totalMana += card.getManaValue();
            for (Character manaColorChar : card.getManacost()) {
                if (color.equals(manaColorChar))
                    colorMana ++;
            };
        }
        return colorMana / totalMana;
    }
/* 
    private static double getSynergy(Deck deck) {
        //Too difficult for this project at the moment, will focus on it in a later iteration.

        return 0;
    }*/

}
