package Utilities;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static Utilities.Card.Suit.*;

public class GameService {
    public static Card[][] divideCards(int numberOfPlayers) throws IllegalArgumentException {
        if (numberOfPlayers != 2 && numberOfPlayers != 4) {
            throw new IllegalArgumentException(
                    "Invalid number of players!!-->>Must be 2 or 4 players.");
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

        Card[][] dividedList = new Card[numberOfPlayers][13]; // 2D array for cards

        if (numberOfPlayers == 2) {
            for (int i = 0, j = 0; i < 52; i += 4, j++) {
                dividedList[0][j] = cards.get(i);
                dividedList[1][j] = cards.get(i + 2);
            }
        } else {
            for (int i = 0, j = 0; i < 52; i += 4, j++) {
                dividedList[0][j] = cards.get(i);
                dividedList[1][j] = cards.get(i + 1);
                dividedList[2][j] = cards.get(i + 2);
                dividedList[3][j] = cards.get(i + 3);
            }
        }
        return dividedList;
    }
    public static Card topCard(ArrayList<Card> cards, Card.Suit hokm) {
        Card.Suit base = cards.get(0).getSuit();
        if (containCard(cards,hokm)){
            return Collections.max(validcard(cards,hokm));
        }
        else{
            return Collections.max(validcard(cards,base));
        }
    }

    public static Card suggestedCard (ArrayList<Card> playedCards,ArrayList<Card> myCards,Card.Suit hokm){
        Card.Suit base = playedCards.get(0).getSuit();
        if((playedCards.size()==1) && (containCard(myCards,base)) && (haveBetterCard(myCards,playedCards.get(0)))){
            return bestCard(myCards,base);
        }
        else if((playedCards.size()==2) && (!containCard(myCards,base)) && (containCard(playedCards,hokm)) && (containCard(myCards,hokm)) && (bestCard(myCards,hokm).compareTo(playedCards.get(1))<0) && (base!= hokm) && (playedCards.get(1).getSuit()==hokm)){
            return worstCard2(myCards,base,playedCards.get(1).getSuit());
        }
        else if ((playedCards.size()==2) && (containCard(myCards,base)) && (!haveBetterCard(myCards,topCard(playedCards,hokm)))  && (!containCard(playedCards,hokm))){
            worstCard1(myCards,base,base);
        }
        else if ((playedCards.size()==3) && (containCard(myCards,base)) && (topCard(playedCards,hokm)==playedCards.get(1)) ){
            worstCard1(myCards,base,base);
        }
        else if((playedCards.size()==2) && (!containCard(myCards,base)) && (containCard(playedCards,hokm)) && (!containCard(myCards,hokm)) &&  (base!= hokm) && (playedCards.get(1).getSuit()==hokm)){
            return worstCard2(myCards,base,playedCards.get(1).getSuit());
        }
        else if((playedCards.size()==2) && (containCard(myCards,base)) && (haveBetterCard(myCards,topCard(playedCards,hokm))) ){
            bestCard(myCards,base);
        }
        else if ((playedCards.size()==3) && (containCard(myCards,base)) && (topCard(playedCards,hokm)!=playedCards.get(1)) && (haveBetterCard(myCards,topCard(playedCards,hokm))) ){
            bestCard(myCards,base);
        }
        else if ((playedCards.size()==3) && (containCard(myCards,base)) && (topCard(playedCards,hokm)!=playedCards.get(1)) ){
            worstCard1(myCards,base,base);
        }
        else if ((playedCards.size()==2) && (!containCard(myCards,base)) && (topCard(playedCards,hokm)!= playedCards.get(0)) && (!containCard(playedCards,hokm))  && (containCard(myCards,hokm))) {          //maybe bad suggest
            worstCard1(myCards,hokm,hokm);
        }
        else if((playedCards.size()==1) && (!containCard(myCards,base)) && (containCard(myCards,hokm)) ){
            worstCard1(myCards,hokm,hokm);
        }
        else{
            return null;
        }
        return null;
    }
    public static Card bestCard(ArrayList<Card> cardarray, Card.Suit cardType) {
        return Collections.max(validcard(cardarray,cardType));
    }
    public static Card worstCard1(ArrayList<Card> cardarray, Card.Suit cardType1,Card.Suit cardType2){
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card:cardarray){
            if ((card.getSuit() == cardType1)||(card.getSuit() == cardType2)) {
                validCards.add(card);
            }
        }
        if (validCards.isEmpty()) {
            return null;
        }
        return Collections.min(validCards);
    }

public static boolean containCard (ArrayList<Card> myCard,Card.Suit cardType){
    for (Card card:myCard){
        if (card.getSuit() == cardType) {
            return true;
        }
    }
    return false;
}

public static boolean haveBetterCard (ArrayList<Card> myCard, Card checkCard){
        if(bestCard(myCard, checkCard.getSuit()).compareTo(checkCard)>0){
            return true;
        }
        else{
            return false;
        }
}

    public static Card worstCard2(ArrayList<Card> cardarray, Card.Suit cardType1,Card.Suit cardType2){
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card : cardarray) {
            if ((card.getSuit() != cardType1) && (card.getSuit() != cardType2)) {
                validCards.add(card);
            }
        }
        if (validCards.isEmpty()) {
            return null;
        } else {
           return Collections.min(validCards);
        }
    }    // do not contain that types
    public static ArrayList<Card> validcard(ArrayList<Card> cards , Card.Suit cardType){
        ArrayList<Card> validCards = new ArrayList<>();
        for (Card card:cards){
            if (card.getSuit() == cardType) {
                validCards.add(card);
            }
        }
        return validCards;
    }

}

