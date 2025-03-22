package com.sjl.mtgai.userLayer;

import java.util.ArrayList;
import java.util.HashMap;

import com.sjl.mtgai.dataLayer.CSVReader;
import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.Tournament;
import com.sjl.mtgai.logicLayer.FeatureEngineering.FeatureAssembler;

import smile.data.DataFrame;

public class userTournamentController {

    private static ArrayList<Tournament> tournaments;
    private static HashMap<Tournament, ArrayList<Deck>> userDecks;

    public static void buildTournaments(String fileLocation, DataCollector collector) {
        tournaments = CSVReader.getUserTournaments(fileLocation, collector);
        userDecks = new HashMap<Tournament, ArrayList<Deck>>();
        for (Tournament tournament : tournaments) {
            userDecks.put(tournament, tournament.getDecks());
        }
    }

    public static ArrayList<Tournament> getTournaments() {
        return tournaments;
    }

    public static DataFrame getRegDataFrame() {
        return FeatureAssembler.assembleRegDataFrame(tournaments);
    }

    public static DataFrame getClassDataFrame() {
        return FeatureAssembler.assembleClassDataFrame(tournaments);
    }

}
