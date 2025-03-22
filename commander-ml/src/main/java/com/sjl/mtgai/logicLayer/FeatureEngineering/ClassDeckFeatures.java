package com.sjl.mtgai.logicLayer.FeatureEngineering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sjl.mtgai.dataLayer.CSVReader;
import com.sjl.mtgai.dataLayer.dataTypes.Card;
import com.sjl.mtgai.dataLayer.dataTypes.Deck;

public class ClassDeckFeatures {

    public static Map<String, Integer> extractFeatures(Deck deck) {
        Map<String, Integer> features = new HashMap<>();
    
        features.put("RankBracket", deck.getRankBracket());
        features.put("ColorCount", getColorCount(deck));
        features.put("CommanderTagSynergy", getCommanderTagSynergy(deck));
        features.put("KeywordSynergy", getKeywordSynergy(deck));
        features.put("ManaCurveSmoothness", getManaCurveSmoothness(deck));
        features.put("TutorCount", getTutorCount(deck));
        features.put("Bracket", getDeckBracket(deck));
        features.put("Tournament", deck.getTournamentID());
        features.put("TypeDominance", getTypeDominance(deck));
        features.put("DeckArhcetype", getDeckArchetype(deck));
        
        return features;
    }

    private static int getColorCount(Deck deck) {
        Map<Character, Integer> colorCount = new HashMap<>();

        for (Card card : deck.getDeckList()) {
            for (char color : card.getColors()) {
                colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
            }
        }

        return colorCount.size();
    }

    private static int getCommanderTagSynergy(Deck deck) {
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
    
        return bucket(supportCount / (double) deck.getDeckList().size()); // proportion of synergy
    }

    private static int getKeywordSynergy(Deck deck) {
        Map<String, Integer> keywordFrequency = new HashMap<>();
        int totalKeywords = 0;
    
        for (Card card : deck.getDeckList()) {
            for (String keyword : card.getKeywords()) {
                keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
                totalKeywords++;
            }
        }
    
        if (totalKeywords == 0) return 0;
    
        // Use a Herfindahl–Hirschman Index (HHI) style calculation: sum of (freq/total)^2
        double synergy = 0.0;
        for (int freq : keywordFrequency.values()) {
            double ratio = (double) freq / totalKeywords;
            synergy += ratio * ratio;
        }
    
        return bucket(synergy);
    }

    private static int getManaCurveSmoothness(Deck deck) {
        List<Double> manaCosts = new ArrayList<>();

        for (Card card : deck.getDeckList()) {
            manaCosts.add(card.getManaValue()); // assumes you have a method that returns numeric mana cost
        }

        if (manaCosts.isEmpty()) return 0;

        double mean = manaCosts.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = manaCosts.stream()
            .mapToDouble(cost -> Math.pow(cost - mean, 2))
            .average()
            .orElse(0.0);

        return bucket(Math.sqrt(variance)); // Standard deviation
    }

    private static int bucket(double ratio) {
        if (0.0 > ratio || ratio == 0.0 ) {
            return 0;            
        } else if (0.0 < ratio && ratio < 0.2) {
            return 1;            
        } else if (0.2 < ratio && ratio < 0.4) {
            return 2;
        } else if (0.4 < ratio && ratio < 0.6) {
            return 3;
        } else if (0.6 < ratio && ratio < 0.8) {
            return 4;
        } else if (0.8 < ratio && ratio < 1.0) {
            return 5;
        } else if (1.0 < ratio) {
            return 5;
        } else {
            return 0;
        }    
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

    private static int getTypeDominance(Deck deck) {
        // Define the types and map their indexes
        String[] allCardTypes = {"Creature", "Sorcery", "Land", "Enchantment", "Instant", "Artifact", "Planeswalker", "Battle"};
        Map<String, Integer> typeIndexMap = new HashMap<>();
        Map<String, Integer> typeCounts = new HashMap<>();
    
        for (int i = 0; i < allCardTypes.length; i++) {
            typeIndexMap.put(allCardTypes[i], i);         // For returning index
            typeCounts.put(allCardTypes[i], 0);           // For counting
        }
    
        for (Card card : deck.getDeckList()) {
            String fullType = card.getType(); // Assuming this is "Legendary Creature - Elf Druid"
            
            for (String type : allCardTypes) {
                // Check if the type is in parsed lists or full string
                if (fullType.contains(type)) {
                    typeCounts.put(type, typeCounts.get(type) + 1);
                }
            }
        }
    
        // Find the type with the highest count
        String dominantType = null;
        int maxCount = -1;
        for (String type : allCardTypes) {
            int count = typeCounts.get(type);
            if (count > maxCount) {
                maxCount = count;
                dominantType = type;
            }
        }
    
        // Return the index of the dominant type
        return (dominantType != null) ? typeIndexMap.get(dominantType) : -1;
    }

    private static int getDeckArchetype(Deck deck) {
        return ArchetypeFeatureExtractor.getDominantArchetype(deck);
    }

}
