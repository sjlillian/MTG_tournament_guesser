package com.sjl.mtgai.logicLayer;

import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.logicLayer.FeatureEngineering.FeatureAssembler;

import smile.data.DataFrame;

public class DataConverter {
    
    public static DataFrame deckDataFrame;
    public static DataFrame userDataFrame;

    public static void buildFrames(DataCollector collector) {
        deckDataFrame = FeatureAssembler.assembleDataFrame(collector.getTournaments());
    }

    public static void buildUserFrames() {
        //userDataFrame = FeatureAssembler.assembleUserDataFrame();
    }
}
