package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

public class TournamentFeatures {

    public static List<Map<String, Double>> extractRegFeatures(Tournament tournament) {
        List<Map<String, Double>> featureList = new ArrayList<>();

        // Compute tournament-level features once
        Map<String, Double> tFeatures = tournamentRegFeatures(tournament);

        for (TournamentEntry entry : tournament.getEntries()) {
            Map<String, Double> features = RegDeckFeatures.extractFeatures(entry.getDeck());

            // Add tournament-level features to this deck's feature map
            features.putAll(tFeatures);

            featureList.add(features);
        }

        return featureList;
    }

    private static Map<String, Double> tournamentRegFeatures(Tournament tournament) {
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


    public static List<Map<String, Integer>> extractClassFeatures(Tournament tournament) {
        List<Map<String, Integer>> featureList = new ArrayList<>();

        // Compute tournament-level features once
        Map<String, Integer> tFeatures = tournamentClassFeatures(tournament);

        for (TournamentEntry entry : tournament.getEntries()) {
            Map<String, Integer> features = ClassDeckFeatures.extractFeatures(entry.getDeck());

            // Add tournament-level features to this deck's feature map
            features.putAll(tFeatures);

            featureList.add(features);
        }

        return featureList;
    }

    private static Map<String, Integer> tournamentClassFeatures(Tournament tournament) {
        Map<String, Integer> features = new HashMap<>();

        features.put("TSameCommander", getSameCommander(tournament));
        return features;
    }

    private static int getSameCommander(Tournament tournament) {
        Map<List<Card>, Integer> commanderFrequency = new HashMap<>();
    
        for (TournamentEntry entry : tournament.getEntries()) {
            commanderFrequency.put(entry.getDeck().getCommander(), commanderFrequency.getOrDefault(entry.getDeck().getCommander(), 0) + 1);
        }
    
        return commanderFrequency.size();
    }

}
