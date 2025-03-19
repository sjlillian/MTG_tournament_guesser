package com.sjl.mtgai.logicLayer;

import java.util.ArrayList;

import com.sjl.mtgai.dataLayer.dataTypes.Tournament;

import smile.data.DataFrame;

public class ConvertTournamentData {

    public static DataFrame getTournamentDataFrame(ArrayList<Tournament> tournaments) {
        String[] columNames = {
            "TournamentID", "WinLoss", "Rank"
        };

        double[][] tournamentData = tournaments.stream()
        .flatMap(tournament -> tournament.getEntries().stream()
            .map(entry -> new double[] {
                tournament.getId(),
                entry.getWinLoss(),
                entry.getRankPercentage()
            }))
        .toArray(double[][]::new);

        System.out.println(tournamentData.length);

        return DataFrame.of(tournamentData, columNames);
    }

}
