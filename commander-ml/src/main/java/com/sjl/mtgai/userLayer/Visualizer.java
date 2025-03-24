package com.sjl.mtgai.userLayer;

import smile.plot.swing.*;
import smile.data.DataFrame;

import javax.swing.*;

import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Visualizer {

    private JFrame frame;
    private JTabbedPane mainTabs;
    private DataCollector collector;
    private DataFrame regDataFrame; // Regression DataFrame
    private DataFrame classDataFrame; // Classification DataFrame
    private Set<Deck> regSet; // Regression Deck Set Order
    private Set<Deck> classSet; // Classification Deck Set Order

    public Visualizer(DataFrame regression, DataFrame classification, DataCollector collector, Set<Deck> regSet, Set<Deck> classSet) {
        this.collector = collector;
        this.regDataFrame = regression;
        this.classDataFrame = classification;
        this.regSet = regSet;
        this.classSet = classSet;
        this.frame = new JFrame("MTG Deck Analysis Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        this.mainTabs = new JTabbedPane();

        addFeatureVsRegressionTab();
        addFeatureVsClassificationTab();
        addCommanderPopularityTab();
        addDeckBreakdownTab();
        addFeatureCorrelationMatrixTab();

        frame.add(mainTabs);
        frame.setVisible(true);
    }

    private void addFeatureVsRegressionTab() {
        String target = "RankPercentage";
        JTabbedPane subTabs = new JTabbedPane();
        for (String feature : regDataFrame.names()) {
            if (!feature.equals(target) && !feature.equals("Tournament"))
                subTabs.add(feature, plotFeatureVsTarget(regDataFrame.drop("Tournament"), feature, target));
        }
        mainTabs.add("Feature vs Regression Target", subTabs);
    }

    private void addFeatureVsClassificationTab() {
        String target = "RankBracket";
        JTabbedPane subTabs = new JTabbedPane();
        for (String feature : classDataFrame.names()) {
            if (!feature.equals(target) && !feature.equals("Tournament"))
                subTabs.add(feature, plotFeatureVsTarget(classDataFrame.drop("Tournament"), feature, target));  
        }
        mainTabs.add("Feature vs Classification Target", subTabs);
    }

    private Component plotFeatureVsTarget(DataFrame df, String feature, String target) {
        ScatterPlot plot = ScatterPlot.of(df, feature, target, '.', Color.BLUE);
        plot.canvas().setAxisLabels(feature, target);
  
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(plot.canvas().panel(), BorderLayout.CENTER);

        return panel;
    }

    private void addCommanderPopularityTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.add("Pair Popularity", getPairPopularity());
        subTabs.add("Single Popularity", getSinglePopularity());
        panel.add(subTabs, BorderLayout.CENTER);
        mainTabs.add("Commander Popularity", panel);
    }

    private Component getPairPopularity() {
        Map<String, Integer> pairPopularity = new HashMap<>();
        for (Deck deck : collector.getDecks()) {
            List<String> names = deck.getCommander().stream()
                         .map(Card::getName)
                         .collect(Collectors.toList());
            Collections.sort(names);
            String key = String.join(" & ", names);
            pairPopularity.put(key, pairPopularity.getOrDefault(key, 0) + 1);
        }

        Map<String, Double> sortedMap = pairPopularity.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().doubleValue(),
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                        ));
    
        String[] labels = sortedMap.keySet().toArray(new String[0]);
        double[][] data = new double[labels.length][1];
        int i = 0;
        for (Double val : sortedMap.values()) {
            data[i++][0] = val;
        }
        BarPlot plot = BarPlot.of(data, labels);
        plot.canvas().setTitle("Commander Pair Popularity");
        plot.canvas().setAxisLabels("Commander Pair", "Frequency");
    
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(plot.canvas().panel(), BorderLayout.CENTER);
        return panel;
    }

    private Component getSinglePopularity() {
        Map<String, Integer> singlePopularity = new HashMap<>();
        for (Deck deck : collector.getDecks()) {
            for (Card commander : deck.getCommander()) {
                singlePopularity.put(commander.getName(), singlePopularity.getOrDefault(commander.getName(), 0) + 1);
            }
        }
    
        Map<String, Double> sortedMap = singlePopularity.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().doubleValue(),
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                        ));
    
        String[] labels = sortedMap.keySet().toArray(new String[0]);
        double[][] data = new double[labels.length][1];
        int i = 0;
        for (Double val : sortedMap.values()) {
            data[i++][0] = val;
        }
        BarPlot plot = BarPlot.of(data, labels);
        plot.canvas().setTitle("Single Commander Popularity");
        plot.canvas().setAxisLabels("Commander", "Frequency");
    
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(plot.canvas().panel(), BorderLayout.CENTER);
        return panel;
    }

    private void addDeckBreakdownTab() {
        DataFrame userClassDataFrame = userTournamentController.getClassDataFrame();
        DataFrame userRegDataFrame = userTournamentController.getRegDataFrame();
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.add("Regression", createDeckTabs(userRegDataFrame.drop("Tournament"), regSet));
        subTabs.add("Classification", createDeckTabs(userClassDataFrame.drop("Tournament"), classSet));
        mainTabs.add("Deck Feature Breakdown", subTabs);
    }

    private Component createDeckTabs(DataFrame df, Set<Deck> decks) {
        JTabbedPane deckTabs = new JTabbedPane();
        String[] featureLabels = df.names();
        double[][] allFeatureValues = df.toArray(featureLabels);
        int i = 0;
        for (Deck deck : decks) {
            double[] deckFeatures = allFeatureValues[i];
    
            // Format into required double[][] shape for BarPlot
            double[][] plotData = new double[deckFeatures.length][1];
            for (int j = 0; j < deckFeatures.length; j++) {
                plotData[j][0] = deckFeatures[j];
            }
    
            // Create bar plot for this deck
            BarPlot plot = BarPlot.of(plotData, featureLabels);
    
            // Add to individual panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(plot.canvas().panel(), BorderLayout.CENTER);
    
            // Add tab with a title
            List<String> names = deck.getCommander().stream()
                         .map(Card::getName)
                         .collect(Collectors.toList());
            Collections.sort(names);
            String commander = String.join(" & ", names);
            String tabTitle = " Deck #" + deck.getId() + " " + commander;
            deckTabs.addTab(tabTitle, panel);
            i++;
        }
    
        return deckTabs;
    }

    private void addFeatureCorrelationMatrixTab() {
        JPanel panel = new JPanel(new BorderLayout());
    
        // Create sub-tabs for Regression and Classification heatmaps
        JTabbedPane subTabs = new JTabbedPane();
    
        // Regression Correlation Heatmap
        subTabs.addTab("Regression Features", getCorrelationHeatMap(regDataFrame.drop("Tournament")));
    
        // Classification Correlation Heatmap
        
        subTabs.addTab("Classification Features", getCorrelationHeatMap(classDataFrame.drop("Tournament")));
    
        // Add sub-tabs to main panel and attach to visualizer
        panel.add(subTabs, BorderLayout.CENTER);
        mainTabs.addTab("Feature Correlation Matrix", panel);
    }

    private JPanel getCorrelationHeatMap(DataFrame df) {
        double[][] corrMatrix = toCorrelationMatrix(df);
        String[] labels = df.names();
        Heatmap featureHeatmap = Heatmap.of(labels, labels, corrMatrix);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(featureHeatmap.canvas().panel(), BorderLayout.CENTER);
        return panel;
    }

    public static double[][] toCorrelationMatrix(DataFrame df) {
        int numFeatures = df.ncol();
        double[][] data = df.toArray(df.names());
        double[][] corrMatrix = new double[numFeatures][numFeatures];

        for (int i = 0; i < numFeatures; i++) {
            for (int j = 0; j < numFeatures; j++) {
                corrMatrix[i][j] = pearsonCorrelation(data[i], data[j]);
            }
        }

        return corrMatrix;
    }

    private static double pearsonCorrelation(double[] x, double[] y) {
        if (x.length != y.length) throw new IllegalArgumentException("Arrays must be the same length.");

        double meanX = Arrays.stream(x).average().orElse(0.0);
        double meanY = Arrays.stream(y).average().orElse(0.0);

        double numerator = 0.0;
        double denominatorX = 0.0;
        double denominatorY = 0.0;

        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            numerator += dx * dy;
            denominatorX += dx * dx;
            denominatorY += dy * dy;
        }

        return (denominatorX == 0 || denominatorY == 0) ? 0.0 : (numerator / Math.sqrt(denominatorX * denominatorY));
    }
}