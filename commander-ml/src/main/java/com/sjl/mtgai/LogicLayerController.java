package com.sjl.mtgai;

import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.logicLayer.Predictor;

public class LogicLayerController {

    private static Predictor randomForest;

    public static void logic() {
        DataConverter.buildFrames(DataLayerController.getCollector());
        randomForest = new Predictor(DataConverter.deckDataFrame);
    }

    public static void getPrediction() {
        randomForest.predict();
    }

}
