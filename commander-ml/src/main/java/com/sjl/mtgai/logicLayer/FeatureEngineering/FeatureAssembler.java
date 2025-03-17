package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;

import smile.data.DataFrame;

public class FeatureAssembler {

    public static DataFrame assembleDataFrame(ArrayList<Deck> decks, ArrayList<Tournament> tournaments) {
        return tournaments.stream()
                .map(tournament -> assemble(tournament))
                .reduce(DataFrame::concat)
                .orElse(null);
    }

    private static DataFrame assemble(Tournament tournament) {
        Map<String, Double> allFeatures = new HashMap<>();

        // allFeatures.putAll(DeckFeatures.extractFeatures(deck));
        allFeatures.putAll(TournamentFeatures.extractFeatures(tournament));
        // allFeatures.putAll(MetaFeatures.extractFeatures(deck, tournament));

        // Convert the map to double[][] and String[]
        double[][] featureValues = new double[1][allFeatures.size()];
        String[] featureNames = new String[allFeatures.size()];

        int index = 0;
        for (Map.Entry<String, Double> entry : allFeatures.entrySet()) {
            featureNames[index] = entry.getKey();
            featureValues[0][index] = entry.getValue();
            index++;
        }

        return DataFrame.of(featureValues, featureNames);
    }
}
