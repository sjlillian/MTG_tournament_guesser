package com.sjl.mtgai.logicLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.RandomForest;

public class RegRandomForestOptimizer {
    private RandomForest model;
    private DataFrame dataFrame;
    private ArrayList<int[]> parameterCombinations; // Combinations of random forest hyperparameters (ntrees, mtry, maxDepth, maxNodes, nodeSize)
    private ArrayList<double[]> performanceMetrics; // Combination of performance metrics (rmse)
    private Random random = new Random();
    
    public RegRandomForestOptimizer(RandomForest model, DataFrame dataFrame) {
        this.model = model;
        this.dataFrame = dataFrame;
        this.parameterCombinations = new ArrayList<int[]>();
        this.performanceMetrics = new ArrayList<double[]>();

        this.parameterCombinations.add(new int[]{
            model.size(), dataFrame.ncol()/3, model.trees()[0].root().depth(), 0, 5
        }); // default parameters
    }
    
    public RandomForest run(int maxRetries) {
        return optimizeModel(model, maxRetries, 0);
    }
    
    private RandomForest optimizeModel(RandomForest model, int maxRetries, int retries) {
        if (retries >= maxRetries || isModelAcceptable(model)) {
            System.out.println(retries + " " + model.metrics().rmse());
            return model;
        }

        performanceMetrics.add(getMetrics(model));
        RandomForest newModel = null;

        switch (retries) {
            case 0: // the first retry
                int[] maxParams = new int[]{
                    random.nextInt(500, 1000), dataFrame.ncol()/2, random.nextInt(30, 50),
                    random.nextInt(10, 50), random.nextInt(10, 20)
                }; // Maximize forest hyperparameters
                RandomForest.Options maxOptions = new RandomForest.Options(
                    maxParams[0], maxParams[1], maxParams[2], maxParams[3], maxParams[4],
                    1.0, null, null);
                newModel = RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame, maxOptions);
                break;
            case 1: // the second retry
                int[] minParams = new int[]{
                    random.nextInt(100, 500), dataFrame.ncol()/4, random.nextInt(3, 5),
                    random.nextInt(2, 5), random.nextInt(2, 5)
                }; // Minimize forest hyperparameters
                RandomForest.Options minOptions = new RandomForest.Options(
                    minParams[0], minParams[1], minParams[2], minParams[3], minParams[4],
                    1.0, null, null);
                newModel = RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame, minOptions);
                break;
            default: // subsequent retries
                newModel  = createNewRandomForest();
                break;
        }
        return optimizeModel(newModel, maxRetries, retries + 1);
    }
    
    private boolean isModelAcceptable(RandomForest model) {
        return model.metrics().r2() > 0.8 && model.metrics().rmse() > 0.75;
    }

    private double[] getMetrics(RandomForest model) {
        double[] metrics = new double[1];
        metrics[0] = model.metrics().rmse(); // rmse
        //metrics[1] = model.metrics().r2(); // r2
        return metrics;
    }
    
    private RandomForest createNewRandomForest() {
        GaussianProcess gp = new GaussianProcess(parameterCombinations, performanceMetrics, 0.01);
        ArrayList<double[]> candidates = getCandidates();
        double bestY = Arrays.stream(gp.y).min().orElse(Double.MAX_VALUE);
        double[] bestCandidate = null;
        double bestEI = -Double.MAX_VALUE;

        for (double[] candidate : candidates) {
            double[] x = Arrays.stream(candidate).toArray();
            double ei = gp.expectedImprovement(x, bestY);
            if (ei > bestEI) {
                bestEI = ei;
                bestCandidate = candidate;
            }
        }

        parameterCombinations.add(new int[]{
            (int)bestCandidate[0], (int)bestCandidate[1], (int)bestCandidate[2],
            (int)bestCandidate[3], (int)bestCandidate[4]
        });

        RandomForest.Options newOptions = new RandomForest.Options((int)bestCandidate[0], (int)bestCandidate[1], (int)bestCandidate[2],
        (int)bestCandidate[3], (int)bestCandidate[4], 1.0, null, null);
        return RandomForest.fit(Formula.lhs("RankPercentage"), dataFrame, newOptions); 
    }

    private ArrayList<double[]> getCandidates() {
        ArrayList<double[]> candidates = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            double ntrees = random.nextDouble(dataFrame.ncol() * 10, 1000);
            double mtry = random.nextDouble(dataFrame.ncol()/4, dataFrame.ncol());
            double maxDepth = random.nextDouble(3, 50);
            double maxNodes = random.nextDouble(2, 50);
            double nodeSize = random.nextDouble(1, 20);
            candidates.add(new double[]{ntrees, mtry, maxDepth, maxNodes, nodeSize});
        }
        return candidates;
    }

    private class GaussianProcess{
        private ArrayList<double[]> X; // Hyperparameter inputs (training points)
        private double[] y;   // Corresponding RMSE scores (targets)
        private double[][] K;     // Covariance matrix
        private double sigmaNoise; // Small noise term for numerical stability

        public GaussianProcess(ArrayList<int[]> X, ArrayList<double[]> y, double sigmaNoise) {
            this.X = toDouble(X);
            this.y = toArray(y);
            this.sigmaNoise = sigmaNoise;
            this.K = computeCovarianceMatrix(this.X);
        }

        private ArrayList<double[]> toDouble(ArrayList<int[]> X) {
            ArrayList<double[]> doubleX = new ArrayList<>();
            for (int[] arr : X) {
                double[] doubleArr = new double[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    doubleArr[i] = (double) arr[i];
                }
                doubleX.add(doubleArr);
            }
            return doubleX;
        }

        private double[] toArray(ArrayList<double[]> y) {
            double[] yArray = new double[y.size()];
            for (int i = 0; i < y.size(); i++) {
                yArray[i] = y.get(i)[0];
            }
            return yArray;
        }

        private double[][] computeCovarianceMatrix(ArrayList<double[]> X) {
            int n = X.size();
            double[][] matrix = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = kernel(X.get(i), X.get(j));
                    if (i == j) matrix[i][j] += sigmaNoise;
                }
            }
            return matrix;
        }

        private double kernel(double[] xi, double[] xj) {
            double sum = 0.0;
            double lengthScale = 1.0;
            for (int i = 0; i < xi.length; i++) {
                double diff = xi[i] - xj[i];
                sum += diff * diff;
            }
            return Math.exp(-sum / (2 * lengthScale * lengthScale));
        }

        public double expectedImprovement(double[] xNew, double bestY) {
            // Predict the mean and variance at the new point
            double mean = predictMean(xNew);
            double variance = predictVariance(xNew);
            double stddev = Math.sqrt(variance);

            // Calculate the improvement
            double improvement = mean - bestY - 1e-6;  // Adding a small value (xi) to encourage exploration
            
            // If the improvement is positive, return the scaled improvement; otherwise, return 0
            return (improvement > 0) ? improvement / stddev : 0.0;
        }
        
        public double predictMean(double[] xNew) {
            int n = X.size();
            double[] kStar = new double[n];
            
            for (int i = 0; i < n; i++) {
                kStar[i] = kernel(xNew, X.get(i));
            }

            double[] alpha = solveLinearSystem(K, y);
            double mean = 0.0;
            for (int i = 0; i < n; i++) {
                mean += kStar[i] * alpha[i];
            }
            return mean;
        }

        private double[] solveLinearSystem(double[][] K, double[] y) {
            int n = K.length;
            double[][] A = new double[n][n];
            double[] b = new double[n];
        
            // Copy and regularize the matrix
            double lambda = 1e-6; // small regularization term
            for (int i = 0; i < n; i++) {
                b[i] = y[i];
                for (int j = 0; j < n; j++) {
                    A[i][j] = K[i][j];
                }
                A[i][i] += lambda; // Add regularization on the diagonal
            }
        
            // Solve A * x = b using Gaussian elimination
            return gaussianEliminationSolve(A, b);
        }
        
        private double[] gaussianEliminationSolve(double[][] A, double[] b) {
            int n = A.length;
            for (int i = 0; i < n; i++) {
                // Find the pivot
                int max = i;
                for (int j = i + 1; j < n; j++) {
                    if (Math.abs(A[j][i]) > Math.abs(A[max][i])) {
                        max = j;
                    }
                }
        
                // Swap rows
                double[] tempRow = A[i];
                A[i] = A[max];
                A[max] = tempRow;
        
                double tempVal = b[i];
                b[i] = b[max];
                b[max] = tempVal;
        
                // Eliminate below
                for (int j = i + 1; j < n; j++) {
                    double factor = A[j][i] / A[i][i];
                    b[j] -= factor * b[i];
                    for (int k = i; k < n; k++) {
                        A[j][k] -= factor * A[i][k];
                    }
                }
            }
        
            // Back substitution
            double[] x = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double sum = b[i];
                for (int j = i + 1; j < n; j++) {
                    sum -= A[i][j] * x[j];
                }
                x[i] = sum / A[i][i];
            }
            return x;
        }
        
        public double predictVariance(double[] xNew) {
            int n = X.size();
    
            // Compute kStar (covariance between new point and training points)
            double[] kStar = new double[n];
            for (int i = 0; i < n; i++) {
                kStar[i] = kernel(xNew, X.get(i));  // k(x_*, x_i)
            }
            
            // Compute the self-covariance k(x_*, x_*)
            double kSelf = kernel(xNew, xNew);
            
            // Compute the variance using the formula
            double variance = kSelf - dotProduct(kStar, solveLinearSystem(K, kStar));
            return variance;
        }

        // Helper method to compute dot product of two vectors
        private double dotProduct(double[] a, double[] b) {
            double result = 0.0;
            for (int i = 0; i < a.length; i++) {
                result += a[i] * b[i];
            }
            return result;
        }
    }

}
