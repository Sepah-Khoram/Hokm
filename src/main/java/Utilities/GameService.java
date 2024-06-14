package Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameService {
    public static Card[][] divideCards(int numberOfPlayers) {
        List<Card> cards = new ArrayList<>(52);
        if (numberOfPlayers != 2 && numberOfPlayers != 4) {
            throw new IllegalArgumentException("Number of invalid players!!-->>Must be 2 or 4 players.");
        }
        int count = 0; // number of cards

        // populate deck with Card objects
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Face face : Card.Face.values())
                cards.add(count++, new Card(face, suit));

        Collections.shuffle(cards); // shuffle deck

        int cardsPerPlayer = 52 / numberOfPlayers;

        Card[][] dividedList = new Card[numberOfPlayers][13];
        for (int i = 0; i < numberOfPlayers ; i++) {
            for (int j = 0; j < cardsPerPlayer ; j++) {

                dividedList[i][j] = cards.get(i * cardsPerPlayer + j);

            }
        }
        return dividedList;
    }
}
