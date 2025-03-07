package com.sjl.mtgai.dataLayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A class that represents 1 tournament entry. An entry consists of one deck and their winning rank at the end of the tournament.
 */

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentEntry {

    private Deck deck;
    private String rank;

}
