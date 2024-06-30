package Server;

import java.util.ArrayList;

public class Team {
    private final ArrayList<Player> players;
    private ArrayList<Set> sets = new ArrayList<>();

    public Team(ArrayList<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return (players.getFirst().getId() + "," + players.getLast().getId());
    }

    public void addSet(Set newSet) {
        this.sets.add(newSet);
    }
}
