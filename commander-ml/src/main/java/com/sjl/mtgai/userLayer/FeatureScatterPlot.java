package com.sjl.mtgai.userLayer;

import smile.plot.swing.ScatterPlot;
import smile.data.DataFrame;

import java.awt.Color;

import javax.swing.*;

public class FeatureScatterPlot {
    public static void plotFeatureVsRank(DataFrame df, String feature) {

        ScatterPlot plot = ScatterPlot.of(df, feature, "RankBracket", '.', Color.BLUE);
        plot.canvas().setAxisLabels(feature, "Rank Bracket");

        JFrame frame = new JFrame("Feature vs RankBracket: " + feature);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(plot.canvas().panel());
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
