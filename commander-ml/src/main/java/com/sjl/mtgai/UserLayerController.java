package com.sjl.mtgai;

import com.sjl.mtgai.logicLayer.DataConverter;
import com.sjl.mtgai.userLayer.FeatureScatterPlot;

public class UserLayerController {

    public static void plotFeatures() {
        FeatureScatterPlot.plotFeatureVsRank(DataConverter.deckDataFrame, "TCommanderDiversity");
    }

}
