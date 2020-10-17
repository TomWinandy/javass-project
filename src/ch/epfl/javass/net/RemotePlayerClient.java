package ch.epfl.javass.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;
import ch.epfl.javass.jass.Card.Color;

/**
 * Represents a player's client.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class RemotePlayerClient implements Player, AutoCloseable {

    private Socket s;
    private BufferedReader r;
    private BufferedWriter w;

    /**
     * Constructor of a RemotePlayerClient. Connects to the given host.
     * 
     * @param hostName
     *            : the name of the host on which the server of the distant
     *            player is running
     * @throws IOException 
     * @throws  
     */
    public RemotePlayerClient(String hostName) throws IOException {
            s = new Socket(hostName, RemotePlayerServer.HOST_PORT);
            r = new BufferedReader(new InputStreamReader(s.getInputStream()));
            w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.CARD.name(),
                    StringSerializer.combine(',',
                            StringSerializer.serializeLong(state.packedScore()),
                            StringSerializer
                                    .serializeLong(state.packedUnplayedCards()),
                            StringSerializer.serializeInt(state.packedTrick())),
                    StringSerializer.serializeLong(hand.packed())));
            w.write("\n");
            w.flush();

            String card = r.readLine();
            return Card.ofPacked(StringSerializer.deserializeInt(card));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Color selectTrump(Random trumpRng) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.SLCT.name(),
                    Integer.toString(1)));
            w.write("\n");
            w.flush();

            String trump = r.readLine();
            return Card.Color.ALL.get(StringSerializer.deserializeInt(trump));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.PLRS.name(),
                    StringSerializer.serializeInt(ownId.ordinal()),
                    StringSerializer.combine(',',
                            StringSerializer.serializeString(
                                    playerNames.get(PlayerId.PLAYER_1)),
                            StringSerializer.serializeString(
                                    playerNames.get(PlayerId.PLAYER_2)),
                            StringSerializer.serializeString(
                                    playerNames.get(PlayerId.PLAYER_3)),
                            StringSerializer.serializeString(
                                    playerNames.get(PlayerId.PLAYER_4)))));
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void updateHand(CardSet newHand) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.HAND.name(),
                    StringSerializer.serializeLong(newHand.packed())));
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public void setTrump(Card.Color trump) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.TRMP.name(),
                    StringSerializer.serializeInt(trump.ordinal())));
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void updateTrick(Trick newTrick) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.TRCK.name(),
                    StringSerializer.serializeInt(newTrick.packed())));
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void updateScore(Score score) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.SCOR.name(),
                    StringSerializer.serializeLong(score.packed())));
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        try {
            w.write(StringSerializer.combine(' ', JassCommand.WINR.name(),
                    StringSerializer.serializeInt(winningTeam.ordinal())));
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws Exception {
        r.close();
        w.close();
        s.close();
    }

}