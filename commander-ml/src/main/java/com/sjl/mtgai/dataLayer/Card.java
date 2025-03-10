package com.sjl.mtgai.dataLayer;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A class that represents a card from Magic: the Gathering. It contains information that can be found on the face of the card.
 * In the case of a "double card", a card that has a layout of , then it will contain a link to the other card as well as the facename
 * which is one "side" of the card.
 */

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    private int id;
    private String name; // Links to the name of the card
    private String facename; // If a double card, this is the name of a single part
    private String type;
    private String keyWords;
    private String colors;
    private int manaValue;
    private ArrayList<Character> manacost;
    private String power;
    private String toughness;
    private String text;
    private Card linkedCard = null; // If a double card, a link to the other Card

    public void linkCard(Card other) {
        this.setLinkedCard(other);
        other.setLinkedCard(this);  // Ensure bidirectional linking
    }

}