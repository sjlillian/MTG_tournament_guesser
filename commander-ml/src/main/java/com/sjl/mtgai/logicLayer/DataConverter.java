package com.sjl.mtgai.logicLayer;

import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.logicLayer.FeatureEngineering.FeatureAssembler;

import smile.data.DataFrame;

public class DataConverter {
    
    public static DataFrame regDataFrame;
    public static DataFrame classDataFrame;

    public static void buildRegFrames(DataCollector collector) {
        regDataFrame = FeatureAssembler.assembleRegDataFrame(collector.getTournaments());
    }

    public static void buildClassFrames(DataCollector collector) {
        classDataFrame = FeatureAssembler.assembleClassDataFrame(collector.getTournaments());
    }
}
