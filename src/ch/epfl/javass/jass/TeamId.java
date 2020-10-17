package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Identifies each of the two teams.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public enum TeamId {
    TEAM_1, TEAM_2;

    /**
     * List of all values of the enumeration, in declaration order.
     */
    public static final List<TeamId> ALL = Collections
            .unmodifiableList(Arrays.asList(values()));

    /**
     * Number of values in the enumeration.
     */
    public static final int COUNT = values().length;

    /**
     * Gives the other team than the one to which it is applied.
     * 
     * @return returns the other team than the one to which it is applied
     */
    public TeamId other() {
        return ALL.get(-this.ordinal() + 1);
    }

}