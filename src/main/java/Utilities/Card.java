// class to represent a Card in a deck of cards
package Utilities;

public class Card implements Comparable<Card> {
    private final Face face;
    private final Suit suit;

    // constructor
    public Card(Face face, Suit suit) {
        this.face = face;
        this.suit = suit;
    }

    @Override
    public int compareTo(Card card) {
        if (this.face.getCode() == 1)
            return 14 - card.face.getCode();

        if (card.face.getCode() == 1)
            return this.face.getCode() - 14;

        return this.face.getCode() - card.face.getCode();
    }

    public Suit getSuit() {
        return suit;
    }

    public enum Face {
        Ace(1), Deuce(2), Three(3), Four(4), Five(5),
        Six(6), Seven(7), Eight(8), Nine(9), Ten(10),
        Jack(11), Queen(12), King(13);

        private final int code;

        Face(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public enum Suit {Clubs, Diamonds, Hearts, Spades}
}
