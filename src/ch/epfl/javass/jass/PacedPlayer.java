package ch.epfl.javass.jass;

import java.util.Map;
import java.util.Random;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * Allows to ensure a player plays not faster than a minimum time.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class PacedPlayer implements Player {

    private final Player underlyingPlayer;
    private final double minTime;
    private final int MILLIS_PER_SECOND = 1000;

    /**
     * Returns a PacedPlayer which behaves like the given player but which never
     * plays a card faster than minTim seconds.
     * 
     * @param underlyingPlayer
     *            the player to have the behavior of
     * @param minTime
     *            the time the program has to wait before the next card can be
     *            played
     */
    public PacedPlayer(Player underlyingPlayer, double minTime) {
        Preconditions.checkArgument(minTime >= 0);
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = minTime;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long startingTime = System.currentTimeMillis();
        Card cardToPlay = underlyingPlayer.cardToPlay(state, hand);
        long timeToPlay = System.currentTimeMillis() - startingTime;
        if (timeToPlay < MILLIS_PER_SECOND * minTime) {
            try {
                Thread.sleep((long) (MILLIS_PER_SECOND * minTime) - timeToPlay);
            } catch (InterruptedException e) {
            }
        }
        return cardToPlay;
    }
    
    @Override
    public Color selectTrump(Random trumpRng) {
        return underlyingPlayer.selectTrump(trumpRng);
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        underlyingPlayer.setPlayers(ownId, playerNames);
    }

    @Override
    public void updateHand(CardSet newHand) {
        underlyingPlayer.updateHand(newHand);
    }

    @Override
    public void setTrump(Card.Color trump) {
        underlyingPlayer.setTrump(trump);
    }

    @Override
    public void updateTrick(Trick newTrick) {
        underlyingPlayer.updateTrick(newTrick);
    }

    @Override
    public void updateScore(Score score) {
        underlyingPlayer.updateScore(score);
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        underlyingPlayer.setWinningTeam(winningTeam);
    }

}
