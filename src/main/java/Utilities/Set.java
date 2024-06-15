package Utilities;

import Server.Player;

import java.security.SecureRandom;

public class Set {
    private String result;
    private Team winner;
    private final Player ruler;
    private int round;

    public Set(Player[] players) {
        round = 0;
        int dealer = new SecureRandom().nextInt(0, players.length); // select dealer
        this.ruler = players[dealer];
    }

    public Player getRuler() {
        return ruler;
    }
}
