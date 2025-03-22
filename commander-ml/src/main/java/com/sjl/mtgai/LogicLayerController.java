package com.sjl.mtgai;

import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.logicLayer.Predictor;

public class LogicLayerController {

    private static Predictor regRandomForest;
    private static Predictor classRandomForest;

    public static void logic() {
        DataConverter.buildRegFrames(DataLayerController.getCollector());
        regRandomForest = new Predictor(DataConverter.regDataFrame);
        DataConverter.buildClassFrames(DataLayerController.getCollector());
        classRandomForest = new Predictor(DataConverter.classDataFrame);
    }

    public static Predictor getRegForest() {
        return regRandomForest;
    }

    public static Predictor getClassForest() {
        return classRandomForest;
    }


}
