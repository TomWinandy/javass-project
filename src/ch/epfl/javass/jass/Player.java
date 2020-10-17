package ch.epfl.javass.jass;

import java.util.Map;
import java.util.Random;

/**
 * Represents a player.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public interface Player {
    /**
     * Returns the card the player wants to play with knowing the current
     * TurnState and the hand of the player.
     * 
     * @param state
     *            the current TurnState
     * @param hand
     *            the cards the player have
     * @return the card the player wants to play
     */
    public Card cardToPlay(TurnState state, CardSet hand);
    
    public Card.Color selectTrump(Random trumpRng);

    /**
     * Called once at the beginning of the game. Tells the player that it has id
     * ownId and that the other's id is set according to the mapping
     * playerNames.
     * 
     * @param ownId
     *            the player's id
     * @param playerNames
     *            the mapping of the other players with their id
     */
    public default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
    }

    /**
     * Called each time the player's hand changes, to let him know his new hand
     * (e.g. each start of turn or after each time the player played a card).
     * 
     * @param newHand
     *            the new hand of the player
     */
    public default void updateHand(CardSet newHand) {
    }

    /**
     * Called each time the trump suit is modified, to let the player know the
     * new trump suit (e.g. start of each turn).
     * 
     * @param trump
     *            the new trump suit
     */
    public default void setTrump(Card.Color trump) {
    }

    /**
     * Called each time the trick is modified (e.g. each time a card is played
     * and each time a trick is collected, replaced by a new empty trick).
     * 
     * @param newTrick the new trick
     */
    public default void updateTrick(Trick newTrick) {
    }
    
    /**
     * Called each time the score changes (e.g. each time a trick is collected).
     * 
     * @param score the new score
     */
    public default void updateScore(Score score) {
    }
    
    /**
     * Called only once, when a team has won the game, having 1000 points or more.
     * 
     * @param winningTeam the winning team
     */
    public default void setWinningTeam(TeamId winningTeam) {
        
    }
}