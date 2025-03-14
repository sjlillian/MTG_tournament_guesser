package com.sjl.mtgai.dataLayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A class that represents 1 tournament entry. An entry consists of one deck and their winning rank at the end of the tournament.
 */

@ToString
@Getter
@Setter
public class TournamentEntry {

    private Deck deck;
    private String rank;
    private double winLoss;
    private double rankPercentage;

    public TournamentEntry(Deck deck, String rank) {
        this.deck = deck;
        this.rank = rank;
        this.winLoss = convertWinLoss(rank);
    }

    private double convertWinLoss(String rank) {
        Pattern pattern = Pattern.compile("(\\d+)%");
        Matcher matcher = pattern.matcher(rank);
        if (matcher.find()) {
            String numberStr = matcher.group(1);
            double percentage = Double.parseDouble(numberStr) / 100.0; // Converts "50" to 0.5
            return percentage;
        } else {
            return Double.NaN;
        }
    }

    public void convertRank(int tournamentSize) {
        // Regular expression to match a number at the start with ordinal suffix (st, nd, rd, th)
        Pattern pattern = Pattern.compile("^(\\d+)(?:st|nd|rd|th)");
        Matcher matcher = pattern.matcher(rank.trim());
        if (matcher.find()) {
            int placement = Integer.parseInt(matcher.group(1));
            // Compute the score using the formula: (N - r + 1) / N
            this.rankPercentage = (tournamentSize - placement + 1) / tournamentSize;
        } else {
            this.rankPercentage =  Double.NaN;
        }
    }

}
