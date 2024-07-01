package Server;

import java.util.ArrayList;

public class Team {
    private final ArrayList<Player> players;
    private final ArrayList<Set> sets = new ArrayList<>();

    public Team(ArrayList<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return (players.getFirst().getId() + "," + players.getLast().getId());
    }

    void addSet(Set newSet) {
        this.sets.add(newSet);
    }

    ArrayList<Player> getPlayers() {
        return players;
    }
}
