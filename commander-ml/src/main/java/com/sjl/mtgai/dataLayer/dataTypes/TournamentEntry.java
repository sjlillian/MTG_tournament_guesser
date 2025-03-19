package com.sjl.mtgai.dataLayer.dataTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
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

    public void convertRank(int actualTournamentSize) {
        Pattern pattern = Pattern.compile("(?:Top)?(\\d+)(?:st|nd|rd|th)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rank.trim());
        if (matcher.find()) {
            int placement = Integer.parseInt(matcher.group(1));
    
            int normalizedSize = normalizeTournamentBracket(actualTournamentSize);
            if (placement > normalizedSize) {
                // Optional: clamp placement so it never exceeds normalized size
                placement = normalizedSize;
            }
    
            this.rankPercentage = 1.0 - ((double)(placement - 1) / normalizedSize);
        } else {
            this.rankPercentage = Double.NaN;
        }
    }

    private int normalizeTournamentBracket(int actualSize) {
        int[] brackets = {4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};
        for (int bracket : brackets) {
            if (actualSize <= bracket) {
                return bracket;
            }
        }
        return actualSize; // fallback, in case of unusually large tournaments
    }

}
