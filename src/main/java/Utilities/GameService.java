package Utilities;

import java.security.SecureRandom;
import java.util.*;

public class GameService {
    public static Card[][] divideCards(int numberOfPlayers) throws IllegalArgumentException {
        if (numberOfPlayers != 2 && numberOfPlayers != 4) {
            throw new IllegalArgumentException(
                    "Invalid number of players!We support just 2 or 4 players.");
        }

        List<Card> cards = new ArrayList<>(52); // deck of cards
        SecureRandom randomNumber = new SecureRandom(); // for generate random number
        int count = 0; // number of cards

        // populate deck with Card objects
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Face face : Card.Face.values())
                cards.add(count++, new Card(face, suit));

        for (int i = 0; i < randomNumber.nextInt(4); i++) {
            Collections.shuffle(cards); // shuffle deck
        }

        Card[][] dividedList; // 2D array for cards

        if (numberOfPlayers == 2) {
            // divide the cards into two decks of 26
            dividedList = new Card[2][26];
            for (int i = 0, j = 0; i < 52; i += 2, j++) {
                dividedList[0][j] = cards.get(i);
                dividedList[0][j] = cards.get(i + 1);
                dividedList[1][j] = cards.get(i + 2);
                dividedList[1][j] = cards.get(i + 3);
            }
        } else {
            // divide the cards into four decks of 13
            dividedList = new Card[4][13];
            for (int i = 0, j = 0; i < 52; i += 4, j++) {
                dividedList[0][j] = cards.get(i);
                dividedList[1][j] = cards.get(i + 1);
                dividedList[2][j] = cards.get(i + 2);
                dividedList[3][j] = cards.get(i + 3);
            }
        }

        return dividedList;
    }

    public static Card suggestedCard(ArrayList<Card> playedCards,
                                               ArrayList<Card> cardsInHand, Card.Suit rule) {
        Card.Suit base = playedCards.getFirst().getSuit();
        if ((playedCards.size() == 1) && (containCard(cardsInHand, base)) && (haveBetterCard(cardsInHand, playedCards.get(0)))) {
            return bestCard(cardsInHand, base);
        } else if (playedCards.isEmpty()) {
            return Collections.max(cardsInHand);
        } else if ((playedCards.size() == 2) && (!containCard(cardsInHand, base)) && (containCard(playedCards, rule)) && (containCard(cardsInHand, rule)) && (bestCard(cardsInHand, rule).compareTo(playedCards.get(1)) < 0) && (base != rule) && (playedCards.get(1).getSuit() == rule)) {
            return worstCardNot(cardsInHand, base, playedCards.get(1).getSuit());
        } else if ((playedCards.size() == 2) && (containCard(cardsInHand, base)) && (!haveBetterCard(cardsInHand, topCard(playedCards, rule))) && (!containCard(playedCards, rule))) {
            return worstCard(cardsInHand, base, base);
        } else if ((playedCards.size() == 3) && (containCard(cardsInHand, base)) && (topCard(playedCards, rule) == playedCards.get(1))) {
            return worstCard(cardsInHand, base, base);
        } else if ((playedCards.size() == 2) && (!containCard(cardsInHand, base)) && (containCard(playedCards, rule)) && (!containCard(cardsInHand, rule)) && (base != rule) && (playedCards.get(1).getSuit() == rule)) {
            return worstCardNot(cardsInHand, base, playedCards.get(1).getSuit());
        } else if ((playedCards.size() == 2) && (containCard(cardsInHand, base)) && (haveBetterCard(cardsInHand, topCard(playedCards, rule)))) {
           return bestCard(cardsInHand, base);
        } else if ((playedCards.size() == 3) && (containCard(cardsInHand, base)) && (topCard(playedCards, rule) != playedCards.get(1)) && (haveBetterCard(cardsInHand, topCard(playedCards, rule)))) {                                          //کارت را دارم کارت بهتر دارم و بازی حکم نیست
            return bestCard(cardsInHand, base);
        } else if ((playedCards.size() == 3) && (containCard(cardsInHand, base)) && (topCard(playedCards, rule) != playedCards.get(1))) {
            return worstCard(cardsInHand, base, base);
        } else if ((playedCards.size() == 2) && (!containCard(cardsInHand, base)) && (topCard(playedCards, rule) != playedCards.get(0)) && (!containCard(playedCards, rule)) && (containCard(cardsInHand, rule))) {          //maybe bad suggest
            return worstCard(cardsInHand, rule, rule);
        } else if ((playedCards.size() == 1) && (!containCard(cardsInHand, base)) && (containCard(cardsInHand, rule))) {
            return worstCard(cardsInHand, rule, rule);
        } else {
            return null;
        }
    }

    private static Card worstCard(ArrayList<Card> cards, Card.Suit cardType1,
                                            Card.Suit cardType2) {
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card : cards) {
            if ((card.getSuit() == cardType1) || (card.getSuit() == cardType2))
                validCards.add(card);
        }
        if (validCards.isEmpty())
            return null;
        else
            return Collections.min(validCards);
    }

    // do not contain type of parameters
    private static Card worstCardNot(ArrayList<Card> cards, Card.Suit cardType1,
                                               Card.Suit cardType2) {
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card : cards) {
            if ((card.getSuit() != cardType1) && (card.getSuit() != cardType2))
                validCards.add(card);
        }
        if (validCards.isEmpty())
            return null;
        else
            return Collections.min(validCards);
    }

    public static Card topCard(ArrayList<Card> cards, Card.Suit rule) {
        Card.Suit base = cards.getFirst().getSuit();
        if (containCard(cards, rule))
            return Collections.max(findCard(cards, rule));
        else
            return Collections.max(findCard(cards, base));
    }

    private static boolean haveBetterCard(ArrayList<Card> cards, Card checkCard) {
        return bestCard(cards, checkCard.getSuit()).compareTo(checkCard) > 0;
    }

    private static Card bestCard(ArrayList<Card> cards, Card.Suit cardType) {
        return Collections.max(findCard(cards, cardType));
    }

    private static boolean containCard(ArrayList<Card> cards, Card.Suit cardType) {
        for (Card card : cards) {
            if (card.getSuit() == cardType)
                return true;
        }
        return false;
    }

    private static ArrayList<Card> findCard(ArrayList<Card> cards,
                                                     Card.Suit cardType) {
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card : cards) {
            if (card.getSuit() == cardType)
                validCards.add(card);
        }
        return validCards;
    }

    public static String[] processNames(String[] names) {
        Map<String, Integer> nameCount = new HashMap<>();
        String[] result = new String[names.length];

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            int count = nameCount.getOrDefault(name, 0) + 1;
            nameCount.put(name, count);
            result[i] = name + count;
        }

        return result;
    }

    public static boolean validCard(ArrayList<Card> playedCards, ArrayList<Card> cardsInHand,
                                    Card checkCard, Card.Suit rule) {
        if (playedCards.isEmpty())
            return true;

        Card.Suit base = playedCards.getFirst().getSuit();
        return (checkCard.getSuit() == base) ||
                (!containCard(cardsInHand, base) &&
                        (checkCard.getSuit() == rule) || !containCard(cardsInHand, rule));
    }
}



