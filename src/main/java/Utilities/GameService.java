package Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameService {
    public static Card[][] divideCards(int numberOfPlayers) {
        List<Card> cards = new ArrayList<>(52);

        int count = 0; // number of cards

        // populate deck with Card objects
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Face face : Card.Face.values())
                cards.add(count++, new Card(face, suit));

        Collections.shuffle(cards); // shuffle deck

        Card[][] dividedList = new Card[numberOfPlayers][13];

    }
}
