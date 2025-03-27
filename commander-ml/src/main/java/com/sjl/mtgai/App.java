package com.sjl.mtgai;

import java.util.List;

import com.sjl.mtgai.userLayer.DeckPredictionAggregate;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        System.out.println("Hello World!");
        //DataLayerController.DBCollector("URL", "USERNAME", "PASSWORD");
        DataLayerController.CSVCollector();
        LogicLayerController.logic();
        UserLayerController.buildUserTournaments("data/tournament_data - trialcsv_1.csv");
        List<DeckPredictionAggregate> predictions = UserLayerController.runPredictions(1);
        UserLayerController.printUserPredictions(predictions, 0);
        // UserLayerController.buildVisualizer(predictions);
        System.out.println("All Done!");
    }
}
