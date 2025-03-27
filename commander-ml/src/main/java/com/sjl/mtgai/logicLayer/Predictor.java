package com.sjl.mtgai.logicLayer;

import smile.data.DataFrame;
import smile.data.formula.Formula;

public class Predictor {

    private smile.regression.RandomForest regRF;
    private smile.classification.RandomForest classRF;
    private ClassRandomForestOptimizer classOptimizer;
    RegRandomForestOptimizer regOptimizer;
    
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

    public void retrain(DataFrame dataFrame) {
        if (isReggression(dataFrame))
            RegressionRetrain(dataFrame);
        else 
            ClassificationRetrain(dataFrame);
    }

    private void RegressionPredictor(DataFrame dataFrame) {
         this.regRF = smile.regression.RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame);
         if (!(regRF.metrics().r2() > 0.8 && regRF.metrics().rmse() < 0.25)) {
            regOptimizer = new RegRandomForestOptimizer(regRF, dataFrame);
            this.regRF = regOptimizer.run(20);
         }
         for (double importance : regRF.importance()) {
             System.out.println(importance);
         }
         System.out.println(regRF.metrics());
    }

    private void RegressionRetrain(DataFrame dataFrame) {
        this.regRF = smile.regression.RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame);
        if (!(regRF.metrics().r2() > 0.8 && regRF.metrics().rmse() < 0.25)) {
            this.regRF = regOptimizer.run(1);
        }
        for (double importance : regRF.importance()) {
            System.out.println(importance);
        }
        System.out.println(regRF.metrics());
   }

    public double[] regPredict(DataFrame dataFrame) {
        return regRF.predict(dataFrame);
    }

    public double[][] regTest(DataFrame dataFrame) {
        return regRF.test(dataFrame);
    }

    private void ClassificationPredictor(DataFrame dataFrame) {
        this.classRF = smile.classification.RandomForest.fit(Formula.lhs("RankBracket"), dataFrame);
        if (!(classRF.metrics().accuracy() > 0.8 && classRF.metrics().error() < 200)) {
            classOptimizer = new ClassRandomForestOptimizer(classRF, dataFrame);
            this.classRF = classOptimizer.run(20);
        }
        for (double importance : classRF.importance()) {
            System.out.println(importance);
        }
        System.out.println(classRF.metrics());
    }

    private void ClassificationRetrain(DataFrame dataFrame) {
        this.classRF = smile.classification.RandomForest.fit(Formula.lhs("RankBracket"), dataFrame);
        if (!(classRF.metrics().accuracy() > 0.8 && classRF.metrics().error() < 200)) {
            this.classRF = classOptimizer.run(1);
        }
        for (double importance : classRF.importance()) {
            System.out.println(importance);
        }
        System.out.println(classRF.metrics());
    }

    public int[] classPredict(DataFrame dataFrame) {
        return classRF.predict(dataFrame);
    }

    public int[][] classTest(DataFrame dataFrame) {
        return classRF.test(dataFrame);
    }

}
