package Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckOfCards {
    private final List<Card> cards; // declare List that will store Cards

    // create cards at the beginning of the program
    public DeckOfCards() {
        cards = new ArrayList<>(52);

        int count = 0; // number of cards

        // populate deck with Card objects
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Face face : Card.Face.values())
                cards.add(count++, new Card(face, suit));

        Collections.shuffle(cards); // shuffle deck
    }

    // player's card
    public DeckOfCards(List<Card> cards) {
        this.cards = cards;
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }
}
