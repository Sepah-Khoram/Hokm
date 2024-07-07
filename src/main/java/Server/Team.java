package Server;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Team {
    private final ArrayList<Player> players;
    private final ArrayList<Set> sets = new ArrayList<>();
    private static final Logger logger = LoggerManager.getLogger();

    public Team(ArrayList<Player> players) {
        this.players = players;
        logger.info("Team created with players: " + players);
    }

    @Override
    public String toString() {
        if (players.size() < 2) {
            logger.warning("Team does not have enough players to get first and last IDs");
            return "";
        }
        String result = players.getFirst().getId() + "," + players.getLast().getId();
        logger.info("Team toString() result: " + result);
        return result;
    }


    void addSet(Set newSet) {
        this.sets.add(newSet);
        logger.info("New set added: " + newSet);
    }

    ArrayList<Player> getPlayers() {
        return players;
    }
}
