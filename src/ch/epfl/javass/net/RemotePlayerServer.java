package ch.epfl.javass.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

/**
 * Represents a player's server, waiting for a connection on the port 5108, and
 * driving a local player according to the received messages.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class RemotePlayerServer {
    private final Player localPlayer;
    // The number of the host port
    public final static int HOST_PORT = 5108;

    /**
     * Constructor of a RemotePlayerServer.
     * 
     * @param localPlayer
     *            : the player of which the server is derived
     */
    public RemotePlayerServer(Player localPlayer) {
        this.localPlayer = localPlayer;
    }

    /**
     * Waits for a message of the client, calls the corresponding methods and
     * possibly sends back a value to the client.
     */
    public void run() {
        try (ServerSocket s0 = new ServerSocket(HOST_PORT);

                // Wait for the message of the client
                Socket s = s0.accept();

                BufferedReader r = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
                BufferedWriter w = new BufferedWriter(
                        new OutputStreamWriter(s.getOutputStream()));) {

            while (true) {
                String line = r.readLine();
                String command = line.substring(0, JassCommand.COMMAND_SIZE);
                String parameter = line.substring(JassCommand.COMMAND_SIZE + 1);

                // Use the corresponding method of the local player
                switch (JassCommand.valueOf(command)) {
                case PLRS:
                    setPlayersHandler(parameter);
                    break;
                case TRMP:
                    localPlayer.setTrump(Card.Color.ALL
                            .get(StringSerializer.deserializeInt(parameter)));
                    break;
                case HAND:
                    localPlayer.updateHand(CardSet.ofPacked(
                            StringSerializer.deserializeLong(parameter)));
                    break;
                case TRCK:
                    localPlayer.updateTrick(Trick.ofPacked(
                            StringSerializer.deserializeInt(parameter)));
                    break;
                case CARD:
                    // Return the returned value of the client
                    w.write(cardToPlayHandler(parameter));
                    w.write("\n");
                    w.flush();
                    break;
                case SCOR:
                    localPlayer.updateScore(Score.ofPacked(
                            StringSerializer.deserializeLong(parameter)));
                    break;
                case WINR:
                    localPlayer.setWinningTeam(TeamId.ALL
                            .get(StringSerializer.deserializeInt(parameter)));
                    break;
                case SLCT:
                    w.write(StringSerializer.serializeInt(localPlayer.selectTrump(null).ordinal()));
                    w.write("\n");
                    w.flush();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Use the method cardToPlay of the local player
    private String cardToPlayHandler(String parameter) {
        String[] parameters = StringSerializer.split(' ', parameter);
        String[] stateInfo = StringSerializer.split(',', parameters[0]);
        CardSet hand = CardSet
                .ofPacked(StringSerializer.deserializeLong(parameters[1]));
        TurnState state = TurnState.ofPackedComponents(
                StringSerializer.deserializeLong(stateInfo[0]),
                StringSerializer.deserializeLong(stateInfo[1]),
                StringSerializer.deserializeInt(stateInfo[2]));
        return StringSerializer
                .serializeInt(localPlayer.cardToPlay(state, hand).packed());
    }

    // Use the method setPlayers of the local player
    private void setPlayersHandler(String parameter) {
        String[] parameters = StringSerializer.split(' ', parameter);
        String[] playersInfo = StringSerializer.split(',', parameters[1]);
        Map<PlayerId, String> playerNames = new HashMap<>();
        for (int i = 0; i < playersInfo.length; i++) {
            playerNames.put(PlayerId.ALL.get(i),
                    StringSerializer.deserializeString(playersInfo[i]));
        }
        localPlayer.setPlayers(
                PlayerId.ALL
                        .get(StringSerializer.deserializeInt(parameters[0])),
                playerNames);
    }

}