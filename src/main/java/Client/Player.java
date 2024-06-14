package Client;

import Utilities.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player extends Client {
    private String name;
    private List<Card> cards;

    public Player(String name) {
        this.name = name;
        this.cards = new ArrayList<>();
    }

    public Player() {
        this("Unknown");
    }

    public String getName() {
        return name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public static void showWinSets() {

    }

    public Card putCard(Card card) {
        if (cards.contains(card)) {
            try {
                getOutput().writeObject("put" + card);
                getOutput().flush();
                String response = (String) getInput().readObject();
                System.out.println("Server responded: " + response);

                cards.remove(card);
                System.out.println("Put Successful");

                return card;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("You have not put a card");
            return null;
        }
    }

    public void showCards() {
        try {
            getOutput().writeObject("showCards");
            getOutput().flush();

            List<Card> response = (List<Card>) getInput().readObject();
            System.out.println("Cards in your hand:");
            for (Card card : response) {
                System.out.println(card);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void recommendCard() {
        try {
            getOutput().writeObject("recommendCard");
            getOutput().flush();

            Card response = (Card) getInput().readObject();
            System.out.println("Recommended card: " + response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}