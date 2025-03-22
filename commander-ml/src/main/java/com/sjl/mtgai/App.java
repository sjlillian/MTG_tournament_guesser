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
        HashMap<Deck, Double> regPredictions = UserLayerController.regPredict(LogicLayerController.getRegForest());
        HashMap<Deck, Integer> classPredictions = UserLayerController.classPredict(LogicLayerController.getClassForest());

        System.out.println("Classification Predictions: ");
        for (Deck deck : classPredictions.keySet()) {
            System.out.println(deck.toString() + "\nPrediction: " + classPredictions.get(deck));
        }

        System.out.println("Regression Predictions: ");
        for (Deck deck : regPredictions.keySet()) {
            System.out.println(deck.toString() + "\nPrediction: " + regPredictions.get(deck));
        }
        // UserLayerController.plotFeatures();
        System.out.println("All Done!");
    }
}
