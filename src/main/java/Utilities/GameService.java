package Utilities;

import Server.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static @Nullable Card suggestedCard(@NotNull ArrayList<Card> playedCards,
                                               ArrayList<Card> cardsInHand, Card.Suit rule) {
        Card.Suit base = playedCards.getFirst().getSuit();
        if ((playedCards.size() == 1) && (containCard(cardsInHand, base)) && (haveBetterCard(cardsInHand, playedCards.get(0)))) {
            return bestCard(cardsInHand, base);
        } else if ((playedCards.size() == 2) && (!containCard(cardsInHand, base)) && (containCard(playedCards, rule)) && (containCard(cardsInHand, rule)) && (bestCard(cardsInHand, rule).compareTo(playedCards.get(1)) < 0) && (base != rule) && (playedCards.get(1).getSuit() == rule)) {
            return worstCardNot(cardsInHand, base, playedCards.get(1).getSuit());
        } else if ((playedCards.size() == 2) && (containCard(cardsInHand, base)) && (!haveBetterCard(cardsInHand, topCard(playedCards, rule))) && (!containCard(playedCards, rule))) {
            worstCard(cardsInHand, base, base);
        } else if ((playedCards.size() == 3) && (containCard(cardsInHand, base)) && (topCard(playedCards, rule) == playedCards.get(1))) {
            worstCard(cardsInHand, base, base);
        } else if ((playedCards.size() == 2) && (!containCard(cardsInHand, base)) && (containCard(playedCards, rule)) && (!containCard(cardsInHand, rule)) && (base != rule) && (playedCards.get(1).getSuit() == rule)) {
            return worstCardNot(cardsInHand, base, playedCards.get(1).getSuit());
        } else if ((playedCards.size() == 2) && (containCard(cardsInHand, base)) && (haveBetterCard(cardsInHand, topCard(playedCards, rule)))) {
            bestCard(cardsInHand, base);
        } else if ((playedCards.size() == 3) && (containCard(cardsInHand, base)) && (topCard(playedCards, rule) != playedCards.get(1)) && (haveBetterCard(cardsInHand, topCard(playedCards, rule)))) {
            bestCard(cardsInHand, base);
        } else if ((playedCards.size() == 3) && (containCard(cardsInHand, base)) && (topCard(playedCards, rule) != playedCards.get(1))) {
            worstCard(cardsInHand, base, base);
        } else if ((playedCards.size() == 2) && (!containCard(cardsInHand, base)) && (topCard(playedCards, rule) != playedCards.get(0)) && (!containCard(playedCards, rule)) && (containCard(cardsInHand, rule))) {          //maybe bad suggest
            worstCard(cardsInHand, rule, rule);
        } else if ((playedCards.size() == 1) && (!containCard(cardsInHand, base)) && (containCard(cardsInHand, rule))) {
            worstCard(cardsInHand, rule, rule);
        } else {
            return null;
        }
        return null;
    }

    private static @Nullable Card worstCard(@NotNull ArrayList<Card> cards, Card.Suit cardType1,
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
    private static @Nullable Card worstCardNot(@NotNull ArrayList<Card> cards, Card.Suit cardType1,
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

    public static Card topCard(@NotNull ArrayList<Card> cards, Card.Suit rule) {
        Card.Suit base = cards.getFirst().getSuit();
        if (containCard(cards, rule))
            return Collections.max(findCard(cards, rule));
        else
            return Collections.max(findCard(cards, base));
    }

    private static boolean haveBetterCard(ArrayList<Card> cards, @NotNull Card checkCard) {
        return bestCard(cards, checkCard.getSuit()).compareTo(checkCard) > 0;
    }

    private static Card bestCard(ArrayList<Card> cards, Card.Suit cardType) {
        return Collections.max(findCard(cards, cardType));
    }

    private static boolean containCard(@NotNull ArrayList<Card> cards, Card.Suit cardType) {
        for (Card card : cards) {
            if (card.getSuit() == cardType)
                return true;
        }
        return false;
    }

    private static @NotNull ArrayList<Card> findCard(@NotNull ArrayList<Card> cards,
                                                     Card.Suit cardType) {
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card : cards) {
            if (card.getSuit() == cardType)
                validCards.add(card);
        }
        return validCards;
    }

    public static void checkPlayerNames(@NotNull List<Player> players) {
        if (players.size() == 2) {
            Player player1 = players.get(0);
            Player player2 = players.get(1);

            String name1 = player1.getName();
            String name2 = player2.getName();

            if (name1.equals(name2)) {
                player1.setName(name1 + "1");
                player2.setName(name2 + "2");
            }
        } else if (players.size() == 4) {
            Player player1 = players.get(0);
            Player player2 = players.get(1);
            Player player3 = players.get(2);
            Player player4 = players.get(3);

            String name1 = player1.getName();
            String name2 = player2.getName();
            String name3 = player3.getName();
            String name4 = player4.getName();

            if (name1.equals(name2)) {
                player1.setName(name1 + "1");
                player2.setName(name2 + "2");
            }
            if (name1.equals(name3) || name2.equals(name3)) {
                player3.setName(name3 + "3");
            }
            if (name1.equals(name4) || name2.equals(name4) || name3.equals(name4)) {
                player4.setName(name4 + "4");
            }
        } else {
            throw new IllegalArgumentException("Invalid number of players! We support just 2 or 4 players.");
        }
    }

    public static boolean validCard(@NotNull ArrayList<Card> playedCards, ArrayList<Card> cardsInHand,
                                    @NotNull Card checkCard, Card.Suit rule) {
        Card.Suit base = playedCards.getFirst().getSuit();
        return (checkCard.getSuit() == base) ||
                (!containCard(cardsInHand, base) &&
                        (checkCard.getSuit() == rule) || !containCard(cardsInHand, rule));
    }
}



