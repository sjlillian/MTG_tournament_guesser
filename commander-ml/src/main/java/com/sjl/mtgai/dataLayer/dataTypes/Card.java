package com.sjl.mtgai.dataLayer.dataTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A class that represents a card from Magic: the Gathering. It contains information that can be found on the face of the card.
 * In the case of a "double card", a card that has a layout of , then it will contain a link to the other card as well as the facename
 * which is one "side" of the card.
 */


@Getter
@Setter
@NoArgsConstructor
public class Card {

    private int id;
    private String name; // Links to the name of the card
    private String facename; // If a double card, this is the name of a single part
    private String type;
    private ArrayList<String> supertypes;
    private ArrayList<String> types;
    private ArrayList<String> subtypes;
    private ArrayList<String> keywords;
    private char[] colors;
    private double manaValue;
    private ArrayList<Character> manacost;
    private String power;
    private String toughness;
    private Boolean gamechanger;
    private String text;
    private Card linkedCard = null; // If a double card, a link to the other Card

    public Card(int id, String name, String facename, String type, ArrayList<String> keywords, char[] colors, double manaValue, ArrayList<Character> manacost, String power, String toughness, Boolean gamechanger, String text, Card linkedCard) {
        this.id = id;
        this.name = name;
        this.facename = facename;
        this.type = type;
        this.keywords = keywords;
        this.colors = colors;
        this.manaValue = manaValue;
        this.manacost = manacost;
        this.power = power;
        this.toughness = toughness;
        this.gamechanger = gamechanger;
        this.text = text;
        this.linkedCard = linkedCard;

        // Call parseType() to initialize supertypes, types, and subtypes
        if (type != null) {
            parseType();
        }
    }

    public void linkCard(Card other) {
        this.setLinkedCard(other);
        other.setLinkedCard(this);  // Ensures bidirectional linking
    }

    public Boolean isGamechanger() {
        return gamechanger;
    }

    public String toString() {
        return new String(this.name);
    }

    private void parseType() {
        // Example: "Legendary Creature - Elf, Druid"
        String[] splitType = type.split(" - ");
        String[] typeParts = splitType[0].split(" "); // "Legendary Creature"

        supertypes = new ArrayList<>();
        types = new ArrayList<>();
        for (String type : typeParts) {
            if (isSupertype(type)) {
                supertypes.add(type);
            } else {
                types.add(type);
            }
        }

        subtypes = (splitType.length > 1) 
            ? new ArrayList<>(Arrays.asList(splitType[1].split(","))) // "Elf, Druid"
            : new ArrayList<>();
    }

    private boolean isSupertype(String type) {
        return List.of("Legendary", "Snow", "Basic", "World", "Ongoing").contains(type);
    }

}