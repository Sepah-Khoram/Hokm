package Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameService {
    public static Card[][] divideCards(int numberOfPlayers) throws IllegalArgumentException {
        if (numberOfPlayers != 2 && numberOfPlayers != 4) {
            throw new IllegalArgumentException(
                    "Invalid number of players!!-->>Must be 2 or 4 players.");
        }

        List<Card> cards = new ArrayList<>(52); // deck of cards

        int count = 0; // number of cards

        // populate deck with Card objects
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Face face : Card.Face.values())
                cards.add(count++, new Card(face, suit));

        Collections.shuffle(cards); // shuffle deck

        Card[][] dividedList = new Card[numberOfPlayers][13];

        if (numberOfPlayers == 2) {
            for (int i = 0, j = 0; i < 52; i += 4, j++) {
                dividedList[0][i] = cards.get(j);
                dividedList[1][i + 2] = cards.get(j);
            }
        } else {
            for (int i = 0, j = 0; i < 52; i += 4, j++) {
                dividedList[0][i] = cards.get(j);
                dividedList[1][i] = cards.get(j);
                dividedList[2][i] = cards.get(j);
                dividedList[3][i] = cards.get(j);
            }
        }

        return dividedList;
    }
}
