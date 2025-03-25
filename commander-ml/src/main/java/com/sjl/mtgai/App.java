package com.sjl.mtgai;

import java.sql.SQLException;
import java.util.List;

import com.sjl.mtgai.userLayer.DeckPredictionAggregate;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws SQLException {
        System.out.println("Hello World!");
        //DataLayerController.DBCollector();
        DataLayerController.CSVCollector();
        LogicLayerController.logic();
        UserLayerController.buildUserTournaments("docs/trial_tournament.csv");
        List<DeckPredictionAggregate> predictions = UserLayerController.runPredictions(100);
        UserLayerController.printUserPredictions(predictions, 0);
        UserLayerController.buildVisualizer(predictions);
        System.out.println("All Done!");
    }
}
