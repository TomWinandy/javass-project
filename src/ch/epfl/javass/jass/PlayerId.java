package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Player of the game
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public enum PlayerId {
    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

    /**
     * List of all values of the enumeration, in declaration order
     */
    public static final List<PlayerId> ALL = Collections
            .unmodifiableList(Arrays.asList(values()));

    /**
     * Number of values in the enumeration
     */
    public static final int COUNT = values().length;

    /**
     * Returns the TeamId of a player.
     * 
     * @return the TeamId of the player the method is called on
     */
    public TeamId team() {
        return TeamId.ALL.get(this.ordinal() % TeamId.COUNT);
    }
}