package com.sjl.mtgai.logicLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sjl.mtgai.dataLayer.Card;
import com.sjl.mtgai.dataLayer.DataCollector;
import com.sjl.mtgai.dataLayer.Deck;
import com.sjl.mtgai.dataLayer.Tournament;

import smile.data.DataFrame;

public class DataConverter {
    
    public static DataFrame deckDataFrame;
    public static DataFrame tournamentDataFrame;
    public static DataFrame deckTournamentDataFrame;

    public static void buildFrames(DataCollector collector) {
        ArrayList<Deck> decks = collector.getDecks();
        ArrayList<Tournament> tournaments = collector.getTournaments();
        deckDataFrame = getDeckDataFrame(decks);
        tournamentDataFrame = getTournamentDataFrame(tournaments);
        deckTournamentDataFrame = deckDataFrame.join(tournamentDataFrame);
    }

    private static DataFrame getTournamentDataFrame(ArrayList<Tournament> tournaments) {
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

    private static DataFrame getDeckDataFrame(ArrayList<Deck> decks) {

        String[] columNames = {
            "ManaCostAvg", "ManaCostMedian", "ManaCostMode", 
            "ManaCostLandRatio", 
            "CreatureNonRatio",
            "typeDiversity", "landRatio",
            "HighestPower", "HighestToughness", "PowerToughnessRatio", "ColorDiversity", "MulticolorRatio", 
            "Gamechangers", 
            "KeywordSynergy", 
            "ManaFixers", 
            "TapLandsRatio", "LifeLossLandsRatio", "LifeGain", "LifeLoss", "CardDraw", 
            "Removals", "Counters",
            "RankPercentage"
        };

        double[][] deckData = decks.stream()
        .map(deck -> new double[] {
            getManaCostAverage(deck),
            getManaCostMedian(deck),
            getManaCostMode(deck),
            getManaCostLandRatio(deck),
            getTypeDiversity(deck),
            getCreatureNonRatio(deck),
            getLandRatio(deck),
            getPowerfullestCreature(deck),
            getToughestCreature(deck),
            getPowerToughnessRatio(deck),
            getColorDiversity(deck),
            getMulticolorRatio(deck),
            getGamechangers(deck),
            getKeywordSynergy(deck),
            getManaFixers(deck),
            getTapLandsRatio(deck),
            getLifeLossLandsRatio(deck),
            getLifeGain(deck),
            getLifeLoss(deck),
            getCardDraw(deck),
            getRemovals(deck),
            getCounters(deck),
            deck.getRankPercentage()
        })
        .toArray(double[][]::new);

        return DataFrame.of(deckData, columNames);
    }

    private static double getManaCostAverage(Deck deck) {
        double average = 0;
        double nonLand = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getManacost() != null) { 
                average += card.getManaValue();
                if (!TextParser(card.getType(), "basic land"))
                    nonLand ++;
            }
        }
        return average / nonLand;
    }

    private static double getManaCostMedian(Deck deck) {
        ArrayList<Integer> manaValues = new ArrayList<>();
        for (Card card : deck.getDeckList()) {
            if (card.getManacost() != null) {
                manaValues.add(card.getManaValue());
            }
        }
        manaValues.sort(null);
        return manaValues.get(manaValues.size() / 2);
    }

    private static double getManaCostMode(Deck deck) {
        Map<Integer, Integer> manaFrequency = new HashMap<>();
        for (Card card : deck.getDeckList()) {
            if (card.getManacost() != null) {
                manaFrequency.put(card.getManaValue(), manaFrequency.getOrDefault(card.getManaValue(), 0) + 1);
            }
        }
        double mode = 0;
        double maxFrequency = 0;
        for (Map.Entry<Integer, Integer> entry : manaFrequency.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                mode = entry.getKey();
                maxFrequency = entry.getValue();
            }
        }
        return mode;
    }

    private static double getManaCostLandRatio(Deck deck) {
        double landCount = 0;
        double totalMana = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getManacost() != null) {
                totalMana += card.getManaValue();
                if (card.getType().contains("Land")) {
                    landCount++;
                }
            }
        }

        return totalMana / landCount;
    }

    private static double getTypeDiversity(Deck deck) {
        String[] knownTypes = {
            "Artifact", "Creature", "Instant", "Sorcery", 
            "Enchantment", "Land", "Planeswalker", "Battlefield"
        };
        
        Map<String, Integer> typesPresent = new HashMap<>();
        for (String type : knownTypes) {
            typesPresent.put(type, 0);
        }

        for (Card card : deck.getDeckList()) {
            String cardTypeInfo = card.getType();
            for (String type : knownTypes) {
                if (cardTypeInfo.contains(type)) {
                    typesPresent.put(type, typesPresent.get(type) + 1);
                }
            }
        }
        
        double countMultiple = 0;
        for (Integer count : typesPresent.values()) {
            if (count > 1) {
                countMultiple++;
            }
        }
        
        return countMultiple / knownTypes.length;
    }

    private static double getCreatureNonRatio(Deck deck) {
        double creatureCount = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getType(), "creature")) {
                creatureCount++;
            }
        }
        return creatureCount / deck.getDeckList().size();
    }

    private static double getLandRatio(Deck deck) {
        double landCount = 0;
        double nonLandCount = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getType(), "land")) {
                landCount++;
            } else {
                nonLandCount++;
            }
        }
        return landCount / nonLandCount;
    }

    private static double getPowerfullestCreature(Deck deck) {
        double power = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getType(), "creature")) {
                if (!TextParser(card.getPower(), "*")) {
                    int powerValue = Integer.parseInt(card.getPower());
                    if (powerValue > power) {
                        power = powerValue;
                    }
                }
            }
        }
        return power;
    }

    
    private static double getToughestCreature(Deck deck) {
        double toughness = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getType(), "creature")) {
                if (!TextParser(card.getToughness(), "*")) {
                    int toughnessValue = Integer.parseInt(card.getToughness());
                    if (toughnessValue > toughness) {
                        toughness = toughnessValue;
                    }
                }
            }
        }
        return toughness;
    }

    private static double getPowerToughnessRatio(Deck deck) {
        double power = 0;
        double toughness = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getType(), "creature")) {
                if (!TextParser(card.getPower(), "*")) {
                    power += Integer.parseInt(card.getPower());
                }
                if (!TextParser(card.getToughness(), "*")) {
                    toughness += Integer.parseInt(card.getToughness());
                }
            }
        }
        
        return power / toughness;
    }

    private static double getColorDiversity(Deck deck) {
        Set<Character> colorsPresent = new HashSet<>();
        for (Card card : deck.getDeckList()) {
            for (char color : card.getColors()) {
             colorsPresent.add(Character.valueOf(color));   
            }
        }
        return colorsPresent.size() / 5.0;
    }

    private static double getMulticolorRatio(Deck deck) {
        double multicolorCount = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getColors().length > 1) {
                multicolorCount++;
            }
        }
        return multicolorCount / deck.getDeckList().size();
    }

    private static double getGamechangers(Deck deck) {
        double amount = 0;
        for (Card card : deck.getDeckList()) {
            if (card.isGamechanger())
                amount ++;            
        }
        return amount;
    }

    private static double getKeywordSynergy(Deck deck) {
        Map<String, Integer> keywordFrequency = new HashMap<>();
        for (Card card : deck.getDeckList()) {
            for (String keyword : card.getKeywords()) {
                keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
            }
        }
        // For synergy, sum extra occurrences (if a keyword appears more than once)
        double synergy = 0;
        for (int count : keywordFrequency.values()) {
            if (count > 1) synergy += (count - 1);
        }
        // Normalize by number of cards (or total keywords)
        return synergy / deck.getDeckList().size();
    }

    private static double getManaFixers(Deck deck) {
        double manaFixers = 0;
        for (Card card : deck.getDeckList()) {
            if (!TextParser(card.getType(), "basic land") && TextParser(card.getText(), "add") && TextParser(card.getText(), "mana")) {
                manaFixers++;
            }
        }
        return manaFixers / deck.getDeckList().size();
    }

    private static double getTapLandsRatio(Deck deck) {
        double tapLands = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getText(), "enters the battlefield tapped")) {
                tapLands++;
            }
        }
        return tapLands / deck.getDeckList().size();
    }

    private static double getLifeLossLandsRatio(Deck deck) {
        double lifeLossLands = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getType(), "land") && TextParser(card.getText(), "lose") && TextParser(card.getText(), "life")) {
                lifeLossLands++;
            }
        }
        return lifeLossLands / deck.getDeckList().size();
    }

    private static double getLifeGain(Deck deck) {
        double lifeGain = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getText(), "gain") && TextParser(card.getText(), "life")) {
                lifeGain ++;
            }
        }
        return lifeGain / deck.getDeckList().size();
    }

    private static double getLifeLoss(Deck deck) {
        double lifeLoss = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getText(), "lose") && TextParser(card.getText(), "life")) {
                lifeLoss ++;
            }
        }
        return lifeLoss / deck.getDeckList().size();
    }

    private static double getCardDraw(Deck deck) {
        double cardDraw = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getText(), "draw") && TextParser(card.getText(), "card")) {
                cardDraw ++;
            }
        }
        return cardDraw / deck.getDeckList().size();
    }

    private static double getRemovals(Deck deck) {
        double removals = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getText(), "destroy") || TextParser(card.getText(), "exile") 
            || TextParser(card.getText(), "sacrifice") || TextParser(card.getText(), "return")) {
                removals ++;
            }
        }
        return removals / deck.getDeckList().size();
    }

    private static double getCounters(Deck deck) {
        double counters = 0;
        for (Card card : deck.getDeckList()) {
            if (TextParser(card.getText(), "counter target")) {
                counters ++;
            }
        }
        return counters / deck.getDeckList().size();
    }

    private static boolean TextParser(String text, String keyword) {
        if (text == null) return false;
        return text.toLowerCase().contains(keyword);
    }

}
