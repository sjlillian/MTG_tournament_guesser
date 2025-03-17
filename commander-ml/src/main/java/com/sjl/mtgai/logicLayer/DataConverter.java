package com.sjl.mtgai.logicLayer;

import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.logicLayer.FeatureEngineering.FeatureAssembler;

import smile.data.DataFrame;

public class DataConverter {
    
    public static DataFrame deckDataFrame;
    public static DataFrame tournamentDataFrame;
    public static DataFrame deckTournamentDataFrame;

    public static void buildFrames(DataCollector collector) {
        deckDataFrame = FeatureAssembler.assembleDataFrame(collector.getDecks(), collector.getTournaments());
    }
}
