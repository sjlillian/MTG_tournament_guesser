package com.sjl.mtgai.logicLayer;

import smile.regression.*;
import smile.data.DataFrame;
import smile.data.formula.Formula;

public class Predictor {

    private DataFrame deckData;
    private RandomForest deckRF;
    
    public Predictor(DataFrame dataFrame) {
        //this.deckData = imputate(dataFrame);
        this.deckRF = RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame);
        if (!(deckRF.metrics().r2() > 0.8 && deckRF.metrics().rmse() < 0.25)) {
            RandomForestOptimizer optimizer = new RandomForestOptimizer(deckRF, dataFrame, 20);
            this.deckRF = optimizer.run();
        }
        for (double importance : deckRF.importance()) {
            System.out.println(importance);
        }
        System.out.println(deckRF.metrics());
    }

    public double[] predict() {
        return deckRF.predict(deckData);
    }

    public double[][] test() {
        return deckRF.test(deckData);
    }

    public double[] predict(DataFrame dataFrame) {
        return deckRF.predict(dataFrame);
    }

    public double[][] test(DataFrame dataFrame) {
        return deckRF.test(dataFrame);
    }

}
