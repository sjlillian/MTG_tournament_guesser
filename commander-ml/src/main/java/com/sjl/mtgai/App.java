package com.sjl.mtgai;

import java.sql.SQLException;
import java.util.HashMap;

import com.sjl.mtgai.dataLayer.dataTypes.Deck;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws SQLException {
        System.out.println("Hello World!");
        DataLayerController.DataCollector();
        LogicLayerController.logic();
        UserLayerController.buildUserTournaments("docs/trial_tournament.csv");
        //HashMap<Deck, Double> deckPredictions = UserLayerController.regPredict(LogicLayerController.getRegForest());
        HashMap<Deck, Integer> deckPredictions = UserLayerController.classPredict(LogicLayerController.getClassForest());

        System.out.println("Predictions: ");
        for (Deck deck : deckPredictions.keySet()) {
            System.out.println(deck.toString() + "\nPrediction: " + deckPredictions.get(deck));
        }
        // UserLayerController.plotFeatures();
        System.out.println("All Done!");
    }
}
