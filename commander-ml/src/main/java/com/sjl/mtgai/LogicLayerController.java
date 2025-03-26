package com.sjl.mtgai;

import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.logicLayer.Predictor;

import smile.data.DataFrame;

public class LogicLayerController {

    private static Predictor regRandomForest;
    private static Predictor classRandomForest;

    private static DataFrame regDataFrame;
    private static DataFrame classDataFrame;

    public static void logic() {
        DataConverter.buildRegFrames(DataLayerController.getCollector());
        regDataFrame = DataConverter.regDataFrame;
        regRandomForest = new Predictor(regDataFrame);
        DataConverter.buildClassFrames(DataLayerController.getCollector());
        classDataFrame = DataConverter.classDataFrame;
        classRandomForest = new Predictor(classDataFrame);
    }

    public static void retrain() {
        regRandomForest.retrain(regDataFrame);
        classRandomForest.retrain(classDataFrame);
    }

    public static Predictor getRegForest() {
        return regRandomForest;
    }

    public static Predictor getClassForest() {
        return classRandomForest;
    }


}
