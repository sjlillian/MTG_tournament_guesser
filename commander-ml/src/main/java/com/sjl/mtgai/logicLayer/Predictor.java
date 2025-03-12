package com.sjl.mtgai.logicLayer;

import smile.regression.*;
import smile.regression.RandomForest.Options;

import java.util.List;
import java.util.stream.Collectors;

import smile.data.DataFrame;
import smile.data.Row;
import smile.data.formula.Formula;
import smile.data.vector.ValueVector;

public class Predictor {

    private DataFrame deckData;
    private RandomForest deckRF;
    
    public Predictor(DataFrame dataFrame) {
        this.deckData = imputate(dataFrame);
        this.deckRF = RandomForest.fit(Formula.lhs("WinLoss"), deckData);
        System.out.println(deckRF.metrics());
    }

    private DataFrame imputate(DataFrame dataFrame) {
        
        DataFrame known = dataFrame.dropna();

        List<Row> unknownRows = dataFrame.stream()
        .filter(row -> Double.isNaN(row.getDouble("WinLoss")))
        .collect(Collectors.toList());

        DataFrame missing = DataFrame.of(dataFrame.schema(), unknownRows);

        // Train a Random Forest regression model for imputation
        RandomForest imputer = RandomForest.fit(Formula.lhs("WinLoss"), known);

        RandomForestTuner tuner = new RandomForestTuner(known);
        imputer = tuner.getModel();
        System.out.println(imputer.metrics());
        
        double[] imputedValues = imputer.predict(missing);

        // Finally, update your original DataFrame with the imputed values as needed
        missing = missing.drop("WinLoss");
        missing.add(ValueVector.of("WinLoss", imputedValues));
        
        return known.concat(missing);
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

    private class RandomForestTuner {
        private RandomForest model;
        private DataFrame trainData;
        private DataFrame testData;
        

        public RandomForestTuner(DataFrame dataFrame) {
            double[][] data = dataFrame.toArray();
            int trainSize = (int) (data.length * 0.5);
            double[][] trainDataArray = new double[trainSize][];
            double[][] testDataArray = new double[data.length - trainSize][];

            this.trainData = DataFrame.of(trainDataArray, dataFrame.names());
            this.testData = DataFrame.of(testDataArray , dataFrame.names());
            this.model = tune();
        }

        private RandomForest tune() {
            RandomForest model = RandomForest.fit(Formula.lhs("WinLoss"), trainData);
            if (model.metrics().r2() < 0.9) {
                Options options = new Options(200, 3);
                model = RandomForest.fit(Formula.lhs("WinLoss"), trainData, options);
            }

            return model;
        }

        public RandomForest getModel() {
            return model;
        }
        
    }

}
