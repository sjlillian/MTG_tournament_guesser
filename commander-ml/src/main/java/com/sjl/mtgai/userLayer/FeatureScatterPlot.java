package com.sjl.mtgai.userLayer;

import smile.plot.swing.ScatterPlot;
import smile.data.DataFrame;

import java.awt.Color;

import javax.swing.*;

public class FeatureScatterPlot {
    public static void plotFeatureVsRank(DataFrame df, String feature) {

        ScatterPlot plot = ScatterPlot.of(df, feature, "RankPercentage", '.', Color.BLUE);
        plot.canvas().setAxisLabels(feature, "Rank Percentage");

        JFrame frame = new JFrame("Feature vs RankPercentage: " + feature);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(plot.canvas().panel());
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
