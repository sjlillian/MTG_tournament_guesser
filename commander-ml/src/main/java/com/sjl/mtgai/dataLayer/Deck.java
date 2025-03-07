package com.sjl.mtgai.dataLayer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A class that represents a deck of cards for the commander format. In a commander deck, there is a total of 100 cards with a sideboard of
 * 15 extra cards. Of these 100 cards, one (or two) is designated the commander. There can be no duplicate cards excdept for basic lands.
 */

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Deck {

    private int id;
    private String commander;
    private String partner; // If the commander allows a partner, NULL otherwise.

    private List<Card> deckList;

}