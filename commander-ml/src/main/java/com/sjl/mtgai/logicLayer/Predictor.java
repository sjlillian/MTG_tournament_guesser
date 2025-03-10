package com.sjl.mtgai.logicLayer;

import smile.classification.*;
import smile.data.DataFrame;
import smile.data.formula.Formula;

public class Predictor {

    private DataFrame deckData;
    private RandomForest deckRF;
    
    public Predictor(DataFrame dataFrame) {
        this.deckData = dataFrame;
        this.deckRF = RandomForest.fit(Formula.lhs("WinLoss"), deckData);
    }

    public int[] predict() {
        return deckRF.predict(deckData);
    }

    public int[][] test() {
        return deckRF.test(deckData);
    }

    public int[] predict(DataFrame dataFrame) {
        return deckRF.predict(dataFrame);
    }

    public int[][] test(DataFrame dataFrame) {
        return deckRF.test(dataFrame);
    }
}
