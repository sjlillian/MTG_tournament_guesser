package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sjl.mtgai.dataLayer.CSVReader;
import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;
import com.sjl.mtgai.dataLayer.dataTypes.TournamentEntry;

public class RegDeckFeatures {

    public static Map<String, Double> extractFeatures(Deck deck) {
        Map<String, Double> features = new HashMap<>();
    
        features.put("RankPercentage", deck.getRankPercentage());
        features.put("CommanderColorRatio", getCommanderColorRatio(deck));
        features.put("CommanderTagSynergy", getCommanderTagSynergy(deck));
        features.put("KeywordSynergy", getKeywordSynergy(deck));
        features.put("ManaCurveSmoothness", getManaCurveSmoothness(deck));
        features.put("TutorCount", (double) getTutorCount(deck));
        features.put("Bracket", (double) getDeckBracket(deck));
        features.put("Tournament", (double) deck.getTournamentID());
        features.put("CommanderPopularity", getCommanderPopularity(deck));
        features.put("DeckSimilarity", getDeckSimilarity(deck));
        features.put("RampPercentage", ArchetypeFeatureExtractor.getRampPercentage(deck));
        features.put("ComboPercentage", ArchetypeFeatureExtractor.getComboPercentage(deck));
        features.put("SpellslingerPercentage", ArchetypeFeatureExtractor.getSpellslingerPercentage(deck));
        features.put("VoltronPercentage", ArchetypeFeatureExtractor.getVoltronPercentage(deck));
        features.put("ControlPercentage", ArchetypeFeatureExtractor.getControlPercentage(deck));
        features.put("AggroPercentage", ArchetypeFeatureExtractor.getAggroPercentage(deck));
        features.put("StaxPercentage", ArchetypeFeatureExtractor.getStaxPercentage(deck));
        features.put("PaddedRatio", deck.getPaddedRatio());
        
        return features;
    }

    private static double getCommanderColorRatio(Deck deck) {
        Map<Character, Integer> colorCount = new HashMap<>();
        int totalColorMentions = 0;

        for (Card card : deck.getDeckList()) {
            for (char color : card.getColors()) {
                colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
                totalColorMentions++;
            }
        }

        if (totalColorMentions == 0) return 0.0;

        int maxColor = Collections.max(colorCount.values());
        return (double) maxColor / totalColorMentions;
    }

    private static double getCommanderTagSynergy(Deck deck) {
        Set<String> commanderKeywords = new HashSet<>();
        Set<String> commanderTribes = new HashSet<>();
    
        for (Card commander : deck.getCommander()) {
            commanderKeywords.addAll(commander.getKeywords());
            commanderTribes.addAll(commander.getSubtypes()); // creature types, like 'Elf', 'Zombie'
        }
    
        int supportCount = 0;
        for (Card card : deck.getDeckList()) {
            if (deck.getCommander().contains(card)) continue; // skip commander
            boolean supportsCommander = false;
    
            for (String keyword : card.getKeywords()) {
                if (commanderKeywords.contains(keyword)) {
                    supportsCommander = true;
                    break;
                }
            }
    
            for (String subtype : card.getSubtypes()) {
                if (commanderTribes.contains(subtype)) {
                    supportsCommander = true;
                    break;
                }
            }
    
            if (supportsCommander) supportCount++;
        }
    
        return supportCount / (double) deck.getDeckList().size(); // proportion of synergy
    }

    private static double getKeywordSynergy(Deck deck) {
        Map<String, Integer> keywordFrequency = new HashMap<>();
        int totalKeywords = 0;
    
        for (Card card : deck.getDeckList()) {
            for (String keyword : card.getKeywords()) {
                keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
                totalKeywords++;
            }
        }
    
        if (totalKeywords == 0) return 0.0;
    
        // Use a Herfindahl–Hirschman Index (HHI) style calculation: sum of (freq/total)^2
        double synergy = 0.0;
        for (int freq : keywordFrequency.values()) {
            double ratio = (double) freq / totalKeywords;
            synergy += ratio * ratio;
        }
    
        return synergy;
    }

    private static double getManaCurveSmoothness(Deck deck) {
        List<Double> manaCosts = new ArrayList<>();

        for (Card card : deck.getDeckList()) {
            manaCosts.add(card.getManaValue()); // assumes you have a method that returns numeric mana cost
        }

        if (manaCosts.isEmpty()) return 0.0;

        double mean = manaCosts.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = manaCosts.stream()
            .mapToDouble(cost -> Math.pow(cost - mean, 2))
            .average()
            .orElse(0.0);

        return Math.sqrt(variance); // Standard deviation
    }

    private static int getTutorCount(Deck deck) {
        int tutorCount = 0;
        
        // Iterate over all cards in the deck
        for (Card card : deck.getDeckList()) {
            String cardText = card.getText(); // Get the card's flavor text or full text
            
            // Check if the card text matches tutor patterns
            if (isTutorCard(cardText)) {
                tutorCount++;
            }
        }
        return tutorCount;
    }
    
    private static boolean isTutorCard(String cardText) {
        if (cardText == null) return false; // Handle null case

        // Define tutor-like keywords/phrases
        String[] tutorKeywords = {
            "search.*library",           // Matches "search" followed by "library"
            "search.*graveyard",         // Matches "search" followed by "graveyard"
            "search.*exile",             // Matches "search" followed by "exile"
            "reveal.*library",           // Matches "reveal" followed by "library"
            "put.*library",              // Matches "put" followed by "library"
            "put.*hand",                 // Matches "put" followed by "hand" (may apply to tutors)
            "put.*battlefield",          // Matches "put" followed by "battlefield"
            "shuffle.*library",          // Matches "shuffle" followed by "library"
            "from.*library.*hand",       // Generic search into hand
            "from.*graveyard.*hand",     // Generic search from graveyard into hand
            "from.*exile.*hand"          // Generic search from exile into hand
        };
        
        // Check if the card text contains any of the tutor-like keywords
        for (String keyword : tutorKeywords) {
            if (cardText.toLowerCase().matches(".*" + keyword + ".*")) {
                return true; // This card matches a tutor pattern
            }
        }
        
        return false; // This card does not match a tutor pattern
    }

    private static double getCommanderPopularity(Deck deck) {
        // Count how many decks share the same commander
        long commanderCount = deck.getTournament().getEntries().stream()
                .filter(e -> e.getDeck().getCommander().equals(deck.getCommander()))
                .count();
        
        // Return commander popularity as the fraction of decks with the same commander
        return commanderCount / (double) deck.getTournament().getEntries().size();
    }

    private static double getDeckSimilarity(Deck deck) {
        // Compare the deck with all other decks in the tournament
        Set<Card> currentDeckCards = new HashSet<>(deck.getDeckList());
        int similarDecks = 0;

        for (TournamentEntry otherEntry : deck.getTournament().getEntries()) {
            if (otherEntry.getDeck().equals(deck)) continue; // Skip the same deck
            Set<Card> otherDeckCards = new HashSet<>(otherEntry.getDeck().getDeckList());
            otherDeckCards.retainAll(currentDeckCards); // Find common cards

            if (!otherDeckCards.isEmpty()) {
                similarDecks++;
            }
        }

        return similarDecks / (double) (deck.getTournament().getEntries().size() - 1); // Exclude the deck itself
    }

    private static int getDeckBracket(Deck deck) {
        int gameChangerCount = countGameChangers(deck);
        int tutorCount = getTutorCount(deck);
        int extraTurnCount = countExtraTurnCards(deck);
        boolean hasInfiniteCombo = hasInfiniteCombo(deck);
        boolean hasMassLandDenial = hasMassLandDenial(deck);

        // Bracket 1: Ultra Casual
        if (gameChangerCount == 0 && tutorCount <= 1 && !hasInfiniteCombo && !hasMassLandDenial && extraTurnCount == 0) {
            return 1;
        }

        // Bracket 2: Precon/Core
        if (gameChangerCount == 0 && tutorCount <= 2 && !hasInfiniteCombo && !hasMassLandDenial && extraTurnCount <= 1) {
            return 2;
        }

        // Bracket 3: Upgraded
        if (gameChangerCount <= 3 && tutorCount <= 4 && !hasMassLandDenial && extraTurnCount <= 2) {
            return 3;
        }

        // Bracket 4: Optimized
        if (gameChangerCount > 3 || tutorCount > 4 || extraTurnCount > 2 || hasInfiniteCombo) {
            return 4;
        }

        // Bracket 5: cEDH — optionally refine this later with synergy/power heuristics
        if (gameChangerCount > 5 && tutorCount > 5 && extraTurnCount > 3 && hasInfiniteCombo) {
            return 5;
        }

        // Fallback in case something slips through
        return 3; // Assume Upgraded as middle ground
    }

    private static int countGameChangers(Deck deck) {
        int count = 0;
        for (Card card : deck.getDeckList()) {
            if (card.getGamechanger()) count++;
        }
        return count;
    }

    private static int countExtraTurnCards(Deck deck) {

        List<String> extraTurnCards = List.of(
            "Alchemist's Gambit", "Alrund's Epiphany", "Capture of Jingzhou", "Chance for Glory", "Emrakul, the Aeons Torn",
            "Expropriate", "Final Fortune", "Gonti's Aether Heart", "Ichormoon Gauntlet", "Last Chance", "Lighthouse Chronologist",
            "Lost Isle Calling", "Magistrate's Scepter", "Magosi, the Waterveil", "Medomai the Ageless", "Mu Yanling",
            "Nexus of Fate", "Notorious Throng", "Part the Waterveil", "Perch Protection", "Phone a Friend", "Plea for Power",
            "Ral Zarek", "Regenerations Restored", "Rise of the Eldrazi", "Sage of Hours", "Savor the Moment", "Search the City",
            "Second Chance", "Seedtime", "Stitch in Time", "Teferi, Timebender", "Temporal Extortion", "Temporal Manipulation",
            "Temporal Mastery", "Temporal Trespass", "Time Sidewalk", "Time Sieve", "Time Vault", "Time Walk", "Timestream Navigator",
            "Twice Upon a Time // Unlikely Meeting", "Ugin's Nexus", "Wanderwine Prophets", "Warrior's Oath", "Wormfang Manta"
        );
        int count = 0;
        for (Card card : deck.getDeckList()) {
            if (extraTurnCards.contains(card.getName())) count++;
        }
        return count;
    }

    private static boolean hasMassLandDenial(Deck deck) {
        List<String> massLandDenialCards = List.of(
        "Ajani Vengeant", "Apocalypse Chime", "The Argent Etchings", "Armageddon", "Bend or Break", "Boil", "Boiling Seas",
        "Bontu's Last Reckoning", "Boom // Bust", "Catastrophe", "Decree of Annihilation", "Desolation Angel", "Devastation",
        "Elesh Norn", "Elspeth Tirel", "Eye of Singularity", "Fall of the Thran", "From the Ashes", "Impending Disaster",
        "Jokulhaups", "Landfill", "Lavaball Trap", "Mine Layer", "Myojin of Infinite Rage", "Obliterate", "Ravages of War",
        "Ruination", "Scourglass", "Solar Tide", "Strategy, Schmategy", "Tsunami", "Urza's Sylex", "Wake of Destruction",
        "Winter's Chill", "Zacama, Primal Calamity"
    );
        boolean hasMassLandDenial = false;
        for (Card card : deck.getDeckList()) {
            if (massLandDenialCards.contains(card.getName())) {
                hasMassLandDenial = true;
                break;
            }
        }
        return hasMassLandDenial;
    }

    private static boolean hasInfiniteCombo(Deck deck) {
        boolean hasInfiniteCombo = false;
        for (Card card : deck.getDeckList()) {
            if (CSVReader.comboMap.containsKey(card.getFacename())) {
                hasInfiniteCombo = true;
                break;
            }
        }
        return hasInfiniteCombo;
    }
}