package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sjl.mtgai.dataLayer.dataTypes.Tournament;

import smile.data.DataFrame;

public class FeatureAssembler {

    public static DataFrame assembleRegDataFrame(ArrayList<Tournament> tournaments) {
        List<Map<String, Double>> allFeatureMaps = tournaments.stream()
                .flatMap(tournament -> TournamentFeatures.extractRegFeatures(tournament).stream())
                .collect(Collectors.toList());

        // Extract column names (keys) from first map
        String[] featureNames = allFeatureMaps.get(0).keySet().toArray(new String[0]);

        // Build a 2D array of values
        double[][] featureValues = new double[allFeatureMaps.size()][featureNames.length];

        for (int i = 0; i < allFeatureMaps.size(); i++) {
            Map<String, Double> map = allFeatureMaps.get(i);
            for (int j = 0; j < featureNames.length; j++) {
                featureValues[i][j] = map.getOrDefault(featureNames[j], 0.0);
            }
        }

        return DataFrame.of(featureValues, featureNames);
    }

    public static DataFrame assembleClassDataFrame(ArrayList<Tournament> tournaments) {
        List<Map<String, Integer>> allFeatureMaps = tournaments.stream()
                .flatMap(tournament -> TournamentFeatures.extractClassFeatures(tournament).stream())
                .collect(Collectors.toList());

        // Extract column names (keys) from first map
        String[] featureNames = allFeatureMaps.get(0).keySet().toArray(new String[0]);

        // Build a 2D array of values
        int[][] featureValues = new int[allFeatureMaps.size()][featureNames.length];

        for (int i = 0; i < allFeatureMaps.size(); i++) {
            Map<String, Integer> map = allFeatureMaps.get(i);
            for (int j = 0; j < featureNames.length; j++) {
                featureValues[i][j] = map.getOrDefault(featureNames[j], 0);
            }
        }

        return DataFrame.of(featureValues, featureNames);
    }
}
