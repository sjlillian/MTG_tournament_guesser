package com.sjl.mtgai.logicLayer;

import smile.regression.*;
import smile.regression.RandomForest.Options;
import smile.validation.CrossValidation;
import smile.validation.metric.R2;

import java.util.List;
import java.util.stream.Collectors;

import smile.data.DataFrame;
import smile.data.Row;
import smile.data.formula.Formula;
import smile.data.vector.ValueVector;
import smile.hpo.Hyperparameters;

public class Predictor {

    private DataFrame deckData;
    private RandomForest deckRF;
    
    public Predictor(DataFrame dataFrame) {
        //this.deckData = imputate(dataFrame);
        this.deckRF = RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame);
        for (double importance : deckRF.importance()) {
            System.out.println(importance);
        }
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

        RandomForestTuner tuner = new RandomForestTuner(known, "WinLoss");
        imputer = tuner.tune();
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

    private DataFrame trainData;
    private String responseVariable;

    public RandomForestTuner(DataFrame trainData, String responseVariable) {
        this.trainData = trainData;
        this.responseVariable = responseVariable;
    }

    public RandomForest tune() {
        // Define the hyperparameters and their ranges
        Hyperparameters hyperparameters = new Hyperparameters();
        hyperparameters.add("ntrees", 50, 500); // Number of trees
        hyperparameters.add("mtry", 1, trainData.ncol() - 1); // Number of features per split

        // Define the objective function to minimize (negative R²)
       // ObjectiveFunction objective = params -> {
        //    int ntrees = params.getInt("ntrees");
        //    int mtry = params.getInt("mtry");

//            RandomForest model = RandomForest.fit(
//                Formula.lhs(responseVariable),
//                trainData,
//                ntrees,
//                mtry
//            );

//            CrossValidation cv = new CrossValidation(trainData.nrows(), 5);
//            double r2 = cv.score(model, trainData, new R2());

//            return -r2; // Minimize negative R²
//        };

        // Perform Bayesian Optimization
//        GridSearch search = new GridSearch(hyperparameters, objective);
//        search.optimize();

        // Retrieve the best hyperparameters
//        int bestNtrees = search.best().getInt("ntrees");
//        int bestMtry = search.best().getInt("mtry");

        // Train the final model with the best hyperparameters
        return RandomForest.fit(
            Formula.lhs(responseVariable),
            trainData
        );
    }
}

}
