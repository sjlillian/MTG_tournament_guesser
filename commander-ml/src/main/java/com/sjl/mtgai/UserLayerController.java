package com.sjl.mtgai;

import java.util.HashMap;
import java.util.Set;

import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.logicLayer.Predictor;
import com.sjl.mtgai.userLayer.Visualizer;
import com.sjl.mtgai.userLayer.userTournamentController;

public class UserLayerController {

    public static void buildUserTournaments(String fileLocation) {
        userTournamentController.buildTournaments(fileLocation, DataLayerController.getCollector());
    }

    public static HashMap<Deck, Double> regPredict(Predictor forest) {
        HashMap<Deck, Double> deckPredictions = new HashMap<Deck, Double>();
        double[] predictions = forest.regPredict(userTournamentController.getRegDataFrame());
        for (Tournament tournament : userTournamentController.getTournaments()) {
            int i = 0;
            for (Deck deck : tournament.getDecks()) {
                deckPredictions.put(deck, Double.valueOf(predictions[i]));
                i++;
            }
        }

        return deckPredictions;
    }

    public static HashMap<Deck, Integer> classPredict(Predictor forest) {
        HashMap<Deck, Integer> deckPredictions = new HashMap<Deck, Integer>();
        int[] predictions = forest.classPredict(userTournamentController.getClassDataFrame());
        for (Tournament tournament : userTournamentController.getTournaments()) {
            int i = 0;
            for (Deck deck : tournament.getDecks()) {
                deckPredictions.put(deck, Integer.valueOf(predictions[i]));
                i++;
            }
        }

        return deckPredictions;
    }

    public static void buildVisualizer(Set<Deck> regSet, Set<Deck> classSet) {
       Visualizer viz = new Visualizer(DataConverter.regDataFrame, DataConverter.classDataFrame, DataLayerController.getCollector(), regSet, classSet);
    }

}
