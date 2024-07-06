// class to represent a Card in a deck of cards
package Utilities;

import java.io.Serializable;

public class Card implements Comparable<Card>, Serializable {
    private final Face face;
    private final Suit suit;

    // constructor
    public Card(Face face, Suit suit) {
        this.face = face;
        this.suit = suit;
    }

    @Override
    public int compareTo(Card card) {
        return suit.compareTo(card.getSuit());
    }

    @Override
    public String toString() {
        return String.format("%s of %s", this.face, this.suit);
    }

    public Suit getSuit() {
        return suit;
    }

    public enum Face {
        Deuce, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace
    }

    public enum Suit {Clubs, Diamonds, Hearts, Spades}
}
