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
            dividedList = new Card[2][];
            dividedList[0] = new Card[25];
            dividedList[1] = new Card[27];
            for (int i = 0, j = 0; i < 50; i += 2, j++) {
                dividedList[0][j] = cards.get(i);
                dividedList[0][j] = cards.get(i + 1);
                dividedList[1][j] = cards.get(i + 2);
                dividedList[1][j] = cards.get(i + 3);
            }
            dividedList[1][25] = cards.get(50);
            dividedList[1][26] = cards.get(51);
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

    private static Card worstCard(ArrayList<Card> cards, Card.Suit cardType1, Card.Suit cardType2) {
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
    private static Card worstCardNot(ArrayList<Card> cards, Card.Suit cardType1, Card.Suit cardType2) {
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

    private static ArrayList<Card> findCard(ArrayList<Card> cards, Card.Suit cardType) {
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

        // First pass: count the occurrences of each name
        for (String name : names) {
            nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
        }

        // Second pass: assign numbers to duplicate names
        Map<String, Integer> nameIndex = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (nameCount.get(name) > 1) {
                int index = nameIndex.getOrDefault(name, 1);
                result[i] = name + index;
                nameIndex.put(name, index + 1);
            } else {
                result[i] = name;
            }
        }

        return result;
    }

    public static boolean validCard(ArrayList<Card> playedCards, ArrayList<Card> cardsInHand, Card checkCard,
                                    Card.Suit rule) {
        if (playedCards.isEmpty())
            return true;

        Card.Suit base = playedCards.getFirst().getSuit();
        return (checkCard.getSuit() == base) || !containCard(cardsInHand, base);
    }

    public static Card suggestedCard(ArrayList<Card> playedCards, ArrayList<Card> cardsInHand, Card teammateCard,
                                     Card.Suit rule) {
        Card.Suit base = playedCards.getFirst().getSuit();

        if (teammateCard != null) {
            if (containCard(playedCards, rule)) {
                if (teammateCard.getSuit() == rule) {
                    if (bestCard(playedCards, rule) == teammateCard) {
                        if (containCard(cardsInHand,base)) {
                           return worstCard(cardsInHand, base, base);
                        } else {
                            return worstCard(cardsInHand, base, rule);
                        }
                    } else {
                        if (containCard(cardsInHand, base)) {
                            return worstCard(cardsInHand, base, base);
                        }
                        else{
                            if(haveBetterCard(cardsInHand,bestCard(playedCards,rule))){
                                return bestCard(cardsInHand,rule);
                            }
                            else{
                                return worstCard(cardsInHand,base,rule);
                            }
                        }
                    }
                }
                else {
                    if(containCard(cardsInHand,base)){
                         return worstCard(cardsInHand,base,base);
                    }
                    else {
                        if(haveBetterCard(cardsInHand,bestCard(playedCards,rule))){
                            return bestCard(cardsInHand,rule);
                        }
                        else {
                            return worstCardNot(cardsInHand,base,rule);
                        }
                    }
                }
            }
            else {
                if(teammateCard==bestCard(playedCards,base)){
                    return worstCard(cardsInHand,base,base);
                }
                else {
                    if (haveBetterCard(cardsInHand,bestCard(playedCards,base))){
                        return bestCard(cardsInHand,base);
                    }
                    else {
                        return worstCard(cardsInHand,base,base);
                    }
                }
            }
        }
        else{
            if(playedCards.isEmpty()){
                return Collections.max(cardsInHand);
            }
            else {
                if(containCard(cardsInHand,base)){
                    if(haveBetterCard(cardsInHand,playedCards.get(0))){
                        return bestCard(cardsInHand,base);
                    }
                    else {
                       return worstCard(cardsInHand,base,base);
                    }
                }
                else {
                    if(containCard(cardsInHand,rule)){
                        return worstCard(cardsInHand,rule,rule);
                    }
                    else {
                        return worstCardNot(cardsInHand,base,rule);
                    }
                }
            }
        }

    }
}



