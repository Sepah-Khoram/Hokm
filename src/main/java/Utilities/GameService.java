package Utilities;

import java.security.SecureRandom;
import java.util.ArrayList;
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
    public static Card topCard (ArrayList<Card> cards, Card.Suit hokm){
        Card.Suit base = cards.get(0).getSuit();
        int counter=0;
        if (cards.size() == 4) {
            if (base == hokm) {
                ArrayList<Card> validCards = new ArrayList<>();
                Card biggestCard;
                for (int i=0 ;i<4;i++){
                    if(cards.get(i).getSuit()==hokm){
                        validCards.add(cards.get(i));
                    }
                }
                biggestCard = validCards.get(0);
                for (int i=0;i<validCards.size();i++){
                    if((i<validCards.size()-1)){
                        if(validCards.get(i).compareTo(validCards.get(i+1))>0){
                            continue;
                        }
                        else{
                            biggestCard=validCards.get(i+1);
                        }
                    }
                    else {
                        return biggestCard;
                    }
                }
            }
            else {
                for (int i = 0; i < 4; i++) {
                    if (cards.get(i).getSuit() == hokm) {
                        counter++;
                    }
                }
                if(counter==0){
                    ArrayList<Card> validCards = new ArrayList<>();
                    Card biggestCard;
                    for (int i=0 ;i<4;i++){
                        if(cards.get(i).getSuit()==base){
                            validCards.add(cards.get(i));
                        }
                    }
                    biggestCard = validCards.get(0);
                    for (int i=0;i<validCards.size();i++){
                        if((i<validCards.size()-1)){
                            if(validCards.get(i).compareTo(validCards.get(i+1))>0){
                                continue;
                            }
                            else{
                                biggestCard=validCards.get(i+1);
                            }
                        }
                        else {
                            return biggestCard;
                        }
                    }
                }
                else if (counter == 1) {
                    for (int i = 0; i < 4; i++) {
                        if (cards.get(i).getSuit() == hokm) {
                            return cards.get(i);
                        }
                    }
                }
                else {
                    ArrayList<Card> cutterCards = new ArrayList<>();
                    for(int i=0 ; i<4;i++){
                        if(cards.get(i).getSuit()==hokm){
                            cutterCards.add(cards.get(i));
                        }
                    }
                    Card biggestCard = cutterCards.get(0);
                    for (int i=0;i<cutterCards.size();i++){
                        if((i<cutterCards.size()-1)){
                            if(cutterCards.get(i).compareTo(cutterCards.get(i+1))>0){
                                continue;
                            }
                            else{
                                biggestCard=cutterCards.get(i+1);
                            }
                        }
                        else {
                            return biggestCard;
                        }
                    }
                }
            }
        }
        else{
            for (int i = 0; i <2; i++) {
                if (cards.get(i).getSuit() == hokm) {
                    counter++;
                }
            }
            if (base==hokm){
                if(counter==1){
                    return cards.get(0);
                }
                else{
                    if(cards.get(0).compareTo(cards.get(1))>0){
                        return cards.get(0);
                    }
                    else{
                        return cards.get(1);
                    }
                }
            }
            else{
                if(counter==1){
                    return cards.get(1);
                }
                else{
                    if(cards.get(0).compareTo(cards.get(1))>0){
                        return cards.get(0);
                    }
                    else{
                        return cards.get(1);
                    }
                }
            }
        }
        return null;
    }
}
