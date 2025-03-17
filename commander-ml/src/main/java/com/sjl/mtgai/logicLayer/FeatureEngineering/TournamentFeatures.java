package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

public class TournamentFeatures {

    public static Map<String, Double> extractFeatures(Tournament tournament) {
        Map<String, Double> features = new HashMap<>();

        for (TournamentEntry entry : tournament.getEntries()) {
            features.putAll(DeckFeatures.extractFeatures(entry.getDeck()));
            features.putAll(tournamentFeatures(tournament));
        }

        return features;
    }

    private static Map<String, Double> tournamentFeatures(Tournament tournament) {
        Map<String, Double> features = new HashMap<>();

        features.put("TCommanderDiversity", getCommanderDiversity(tournament));
        return features;
    }

    private static double getCommanderDiversity(Tournament tournament) {
        Map<List<Card>, Integer> commanderFrequency = new HashMap<>();
    
        for (TournamentEntry entry : tournament.getEntries()) {
            commanderFrequency.put(entry.getDeck().getCommander(), commanderFrequency.getOrDefault(entry.getDeck().getCommander(), 0) + 1);
        }
    
        return (double) commanderFrequency.size() / tournament.getEntries().size();
    }

}
