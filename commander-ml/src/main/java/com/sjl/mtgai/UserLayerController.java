package com.sjl.mtgai;

import java.util.HashMap;

import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.logicLayer.Predictor;
import com.sjl.mtgai.userLayer.FeatureScatterPlot;
import com.sjl.mtgai.userLayer.userTournamentController;

public class UserLayerController {

    public static void buildUserTournaments(String fileLocation) {
        userTournamentController.buildTournaments(fileLocation, DataLayerController.getCollector());
    }

    public static HashMap<Deck, Double> predict(Predictor forest) {
        HashMap<Deck, Double> deckPredictions = new HashMap<Deck, Double>();
        double[] predictions = forest.predict(userTournamentController.getDataFrame());
        for (Tournament tournament : userTournamentController.getTournaments()) {
            int i = 0;
            for (Deck deck : tournament.getDecks()) {
                deckPredictions.put(deck, Double.valueOf(predictions[i]));
                i++;
            }
        }
        

        return deckPredictions;
    }

    public static void plotFeatures() {
        FeatureScatterPlot.plotFeatureVsRank(DataConverter.deckDataFrame, "CommanderPopularity");
    }

}
