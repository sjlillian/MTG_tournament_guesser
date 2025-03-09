package com.sjl.mtgai.logicLayer;

import java.util.ArrayList;

import com.sjl.mtgai.dataLayer.Card;
import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.dataLayer.Deck;

import smile.data.DataFrame;
import smile.data.Dataset;
import smile.data.vector.DoubleVector;

public class DataConverter {

    public DataConverter() {
        // I need to collect data about the decks, and then build DataFrames.


    }

    public static void buildFrames(DataCollector collector) {
        ArrayList<Deck> decks = collector.getDecks();
        ArrayList<ArrayList<Float>> deckData = new ArrayList<ArrayList<Float>>();

        for (Deck deck : decks) {
            ArrayList<Float> data = new ArrayList<Float>();
            data.add(getManaCostAverage(deck));
            data.add(getTypeRatio(deck, "Creature"));
            data.add(getTypeRatio(deck, "Sorcery"));
            data.add(getTypeRatio(deck, "Instant"));
            data.add(getTypeRatio(deck, "Artifact"));
            data.add(getTypeRatio(deck, "Enchantment"));
            data.add(getTypeRatio(deck, "Land"));
            data.add(getTypeRatio(deck, "Planeswalker"));
            data.add(getColor(deck, "W"));
            data.add(getColor(deck, "U"));
            data.add(getColor(deck, "B"));
            data.add(getColor(deck, "R"));
            data.add(getColor(deck, "G"));
            data.add(getColor(deck, "null"));
            //data.add(getSynergy(deck));
            //data.add(deck.win%);
            deckData.add(data);
        }

        //DataFrame deckDataFrame = Dataset.of(deckData);

    }

    private static float getManaCostAverage(Deck deck) {
        float average = 0;
        int nonLand = 0;
        for (Card card : deck.getDeckList()) {
            average += card.getManaValue();
            if (!card.getType().equals("Basic Land"))
                nonLand ++;
        }
        return average / nonLand;
    }

    private static float getTypeRatio(Deck deck, String type) {
        float total = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getType().equals(type))
                total ++;
        }
        return total / deck.getDeckList().size();
    }

    private static float getColor(Deck deck, String color) {
        float colorMana = 0;
        float totalMana = 0;
        for (Card card : deck.getDeckList()) {
            totalMana += card.getManaValue();
            if (card.getManacost().equals(color));
                //colorMana += card.getManacost();
        }
        return colorMana / totalMana;
    }

    private static float getSynergy(Deck deck) {
        return 0;
    }

}
