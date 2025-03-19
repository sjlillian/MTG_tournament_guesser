package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

public class TournamentFeatures {

    public static List<Map<String, Double>> extractFeatures(Tournament tournament) {
        List<Map<String, Double>> featureList = new ArrayList<>();

        // Compute tournament-level features once
        Map<String, Double> tFeatures = tournamentFeatures(tournament);

        for (TournamentEntry entry : tournament.getEntries()) {
            Map<String, Double> features = DeckFeatures.extractFeatures(entry.getDeck());

            // Add tournament-level features to this deck's feature map
            features.putAll(tFeatures);

            featureList.add(features);
        }

        return featureList;
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
