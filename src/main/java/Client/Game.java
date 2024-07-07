package Client;

public record Game(String token, int numberOfPlayers, int connectedPlayers) {
    @Override
    public String toString() {
        return String.format("%40s        %1d    %12d", token, numberOfPlayers, connectedPlayers);
    }
}
