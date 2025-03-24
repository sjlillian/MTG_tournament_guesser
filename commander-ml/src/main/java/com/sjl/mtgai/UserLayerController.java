package com.sjl.mtgai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.userLayer.DeckPredictionAggregate;
import com.sjl.mtgai.userLayer.Visualizer;
import com.sjl.mtgai.userLayer.userTournamentController;

public class UserLayerController {

    public static void buildUserTournaments(String fileLocation) {
        userTournamentController.buildTournaments(fileLocation, DataLayerController.getCollector());
    }

    public static List<DeckPredictionAggregate> runPredictions(int repetitions) {
        Map<Deck, DeckPredictionAggregate> aggregates = new HashMap<>();

        for (int r = 0; r < repetitions; r++) {
            // Regression predictions
            double[] reg = LogicLayerController.getRegForest().regPredict(userTournamentController.getRegDataFrame());

            // Classification predictions
            int[] clas = LogicLayerController.getClassForest().classPredict(userTournamentController.getClassDataFrame());

            int i = 0;
            for (Tournament t : userTournamentController.getTournaments()) {
                for (Deck d : t.getDecks()) {
                    aggregates.putIfAbsent(d, new DeckPredictionAggregate(d));
                    aggregates.get(d).addPredictions(clas[i], reg[i]);
                    i++;
                }
            }
        }

        return new ArrayList<>(aggregates.values());
    }

    public static void printUserPredictions(List<DeckPredictionAggregate> predictions, int topN) {
        if (topN <= 0) topN = predictions.size();
        predictions.sort(Comparator.comparingDouble(p -> -p.getCompositeScore(0.7, 0.3))); // Sort by score descending
    
        System.out.println("\nFinal User Tournament Predictions:");
        for (DeckPredictionAggregate dp : predictions) {
            System.out.println(dp.getSummary());
        }
    
        System.out.println("\nTop " + topN + " decks most likely to win:");
        for (int i = 0; i < Math.min(topN, predictions.size()); i++) {
            DeckPredictionAggregate top = predictions.get(i);
            System.out.printf("[%d] %s | Score: %.3f | Confidence: %.3f%n", i + 1,
                top.getDeck().getCommanderNames(), top.getCompositeScore(0.7, 0.3), top.getCompositeConfidence(0.5, 0.5));
        }
    }

    public static void buildVisualizer(List<DeckPredictionAggregate> predictions) {
        HashSet<Deck> decks = new HashSet<Deck>();
        for (DeckPredictionAggregate prediction : predictions) {
            decks.add(prediction.getDeck());            
        }
        new Visualizer(DataConverter.regDataFrame, DataConverter.classDataFrame, DataLayerController.getCollector(), decks);
    }

}
