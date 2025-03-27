package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.HashMap;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;

public class ArchetypeFeatureExtractor {

    public static int getDominantArchetype(Deck deck) {
        Map<Integer, Double> archetypeMap = new HashMap<>();
        archetypeMap.put(0, getRampPercentage(deck));
        archetypeMap.put(1, getComboPercentage(deck));
        archetypeMap.put(2, getSpellslingerPercentage(deck));
        archetypeMap.put(3, getVoltronPercentage(deck));
        archetypeMap.put(4, getControlPercentage(deck));
        archetypeMap.put(5, getAggroPercentage(deck));
        archetypeMap.put(6, getStaxPercentage(deck));

        return archetypeMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey(); // Return the index of highest %.
    }


    public static double getRampPercentage(Deck deck) {
        int rampCount = 0;
        for (Card card : deck.getDeckList()) {
            if (containsRampIndicators(card)) rampCount++;
        }
        return (double) rampCount / deck.getDeckList().size();
    }

    public static double getComboPercentage(Deck deck) {
        int comboCount = 0;
        for (Card card : deck.getDeckList()) {
            if (containsComboIndicators(card)) comboCount++;
        }
        return (double) comboCount / deck.getDeckList().size();
    }

    public static double getSpellslingerPercentage(Deck deck) {
        int spellslingerCount = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getType().contains("Instant") || card.getType().contains("Sorcery")) spellslingerCount++;
        }
        return (double) spellslingerCount / deck.getDeckList().size();
    }

    public static double getVoltronPercentage(Deck deck) {
        int voltronCount = 0;
        for (Card card : deck.getDeckList()) {
            if (containsVoltronIndicators(card)) voltronCount++;
        }
        return (double) voltronCount / deck.getDeckList().size();
    }

    public static double getControlPercentage(Deck deck) {
        int controlCount = 0;
        for (Card card : deck.getDeckList()) {
            if (containsControlIndicators(card)) controlCount++;
        }
        return (double) controlCount / deck.getDeckList().size();
    }

    public static double getAggroPercentage(Deck deck) {
        int aggroCount = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getType().contains("Creature") && card.getManaValue() <= 3) aggroCount++;
        }
        return (double) aggroCount / deck.getDeckList().size();
    }

    public static double getStaxPercentage(Deck deck) {
        int staxCount = 0;
        for (Card card : deck.getDeckList()) {
            if (containsStaxIndicators(card)) staxCount++;
        }
        return (double) staxCount / deck.getDeckList().size();
    }

    private static boolean containsRampIndicators(Card card) {
        if (card.getText() == null) return false;
        String text = card.getText().toLowerCase();
        return text.contains("add {") || text.contains("search your library for a land") || text.contains("mana pool");
    }

    private static boolean containsComboIndicators(Card card) {
        if (card.getText() == null) return false;
        String text = card.getText().toLowerCase();
        return text.contains("untap") || text.contains("infinite") || text.contains("Combo");
    }

    private static boolean containsVoltronIndicators(Card card) {
        if (card.getText() == null) return false;
        String text = card.getText().toLowerCase();
        return card.getType().contains("Aura") || card.getType().contains("Equipment") || text.contains("equip") || text.contains("+1/+1") || text.contains("+X/+X");
    }

    private static boolean containsControlIndicators(Card card) {
        if (card.getText() == null) return false;
        String text = card.getText().toLowerCase();
        return card.getType().contains("Instant") || card.getType().contains("Sorcery") || text.contains("counter") || text.contains("destroy");
    }

    private static boolean containsStaxIndicators(Card card) {
        if (card.getText() == null) return false;
        String text = card.getText().toLowerCase();
        return text.contains("each player") || text.contains("skip your") || text.contains("sacrifice a");
    }
}
