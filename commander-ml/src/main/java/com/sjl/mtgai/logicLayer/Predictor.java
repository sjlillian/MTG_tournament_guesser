package com.sjl.mtgai.logicLayer;

import smile.data.DataFrame;
import smile.data.formula.Formula;

public class Predictor {

    private smile.regression.RandomForest deckRRF;
    private smile.classification.RandomForest deckCRF;
    
    public Predictor(DataFrame dataFrame) {
        if (isReggression(dataFrame))
            RegressionPredictor(dataFrame);
        else 
            ClassificationPredictor(dataFrame);
    }

    private boolean isReggression(DataFrame dataFrame) {
        for (String name : dataFrame.names()) {
            if (name.equals("RankPercentage"))
                return true;
        }
        return false;
    }

    private void RegressionPredictor(DataFrame dataFrame) {
        
         this.deckRRF = smile.regression.RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame);
         if (!(deckRRF.metrics().r2() > 0.8 && deckRRF.metrics().rmse() < 0.25)) {
             RandomForestOptimizer optimizer = new RandomForestOptimizer(deckRRF, dataFrame, 20);
             this.deckRRF = optimizer.run();
         }
         for (double importance : deckRRF.importance()) {
             System.out.println(importance);
         }
         System.out.println(deckRRF.metrics());
    }

    public double[] regPredict(DataFrame dataFrame) {
        return deckRRF.predict(dataFrame);
    }

    public double[][] regTest(DataFrame dataFrame) {
        return deckRRF.test(dataFrame);
    }

    private void ClassificationPredictor(DataFrame dataFrame) {
        this.deckCRF = smile.classification.RandomForest.fit(Formula.lhs("RankBracket"), dataFrame);
        for (double importance : deckCRF.importance()) {
            System.out.println(importance);
        }
        System.out.println(deckCRF.metrics());
    }

    public int[] classPredict(DataFrame dataFrame) {
        return deckCRF.predict(dataFrame);
    }

    public int[][] classTest(DataFrame dataFrame) {
        return deckCRF.test(dataFrame);
    }

}
