package com.sjl.mtgai.userLayer;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import com.sjl.mtgai.dataLayer.dataTypes.Deck;

public class DeckPredictionAggregate {

    private Deck deck;
    private ArrayList<Double> regPredictions;
    private ArrayList<Integer> classPredictions;

    public DeckPredictionAggregate(Deck deck) {
        this.deck = deck;
        this.regPredictions = new ArrayList<Double>();
        this.classPredictions = new ArrayList<Integer>(); 
    }

    public void addPredictions(int classPrediction, double regPrediction) {
        classPredictions.add(classPrediction);
        regPredictions.add(regPrediction);
    }

    public int getMostCommonBracket() {
        return classPredictions.stream()
            .collect(Collectors.groupingBy(b -> b, Collectors.counting()))
            .entrySet().stream().max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(0);
    }

    public double getAverageRankPercentage() {
        return regPredictions.stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public Deck getDeck() {
        return this.deck;
    }

    public double getCompositeScore(double weightRegression, double weightBracket) {
        double normalizedBracket = 1.0 - (getMostCommonBracket() / 5.0); // Assuming 5 Brackets max
        return (weightRegression * getAverageRankPercentage()) + (weightBracket * normalizedBracket);
    }

    public double getRegressionStdDev() {
        double mean = getAverageRankPercentage();
        return Math.sqrt(regPredictions.stream()
            .mapToDouble(p -> Math.pow(p - mean, 2))
            .average().orElse(0.0));
    }
    
    public double getBracketConfidence() {
        // % of times the most common bracket was predicted
        int mode = getMostCommonBracket();
        long count = classPredictions.stream().filter(b -> b == mode).count();
        return (double) count / classPredictions.size(); // range: 0 to 1
    }

    public double getCompositeConfidence(double weightRegConf, double weightBracketConf) {
        double regConfidence = 1.0 - getRegressionStdDev(); // lower stddev = higher confidence
        double bracketConfidence = getBracketConfidence();
        return (weightRegConf * regConfidence) + (weightBracketConf * bracketConfidence);
    }

    public String getSummary() {
        return String.format(
            "Deck: %s | Avg Rank%%: %.3f | Most Common Bracket: %d | Score: %.3f | Confidence: %.3f",
            deck.getCommanderNames(),
            getAverageRankPercentage(),
            getMostCommonBracket(),
            getCompositeScore(0.7, 0.3),
            getCompositeConfidence(0.5, 0.5)
        );
    }

}
