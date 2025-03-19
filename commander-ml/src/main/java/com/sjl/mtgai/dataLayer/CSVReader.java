package com.sjl.mtgai.dataLayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CSVReader {

    public static Map<String, Set<String>> comboMap = new HashMap<>();

    public static void buildComboMap() {
        String filePath = "database_files/infinite_combos.csv";  // Path to the CSV file

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Loop through each line in the CSV file
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");  // Split the line by commas

                String keyCard = tokens[0].trim();  // The first card is the key card
                
                // Add the combo cards for this key card
                for (int i = 1; i < tokens.length; i++) {
                    String comboCard = tokens[i].trim();  // Combo card is at index i
                    
                    // If the keyCard isn't already in the map, initialize the set for it
                    comboMap.putIfAbsent(keyCard, new HashSet<>());
                    
                    // Add the combo card to the set of combos for the key card
                    comboMap.get(keyCard).add(comboCard);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
