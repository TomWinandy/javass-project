package ch.epfl.javass.gui;

import java.util.EnumMap;
import java.util.Map;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * JavaFX bean containing the scores.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class ScoreBean {
    private final Map<TeamId, IntegerProperty> turnPoints = new EnumMap<>(
            TeamId.class);
    private final Map<TeamId, IntegerProperty> gamePoints = new EnumMap<>(
            TeamId.class);
    private final Map<TeamId, IntegerProperty> totalPoints = new EnumMap<>(
            TeamId.class);
    private final ObjectProperty<TeamId> winningTeam = new SimpleObjectProperty<TeamId>();

    public ScoreBean() {
        init(turnPoints);
        init(gamePoints);
        init(totalPoints);
    }

    private void init(Map<TeamId, IntegerProperty> map) {
        for (TeamId team : TeamId.ALL) {
            map.put(team, new SimpleIntegerProperty());
        }
    }

    /**
     * Returns the turnPoints property of the given team.
     * 
     * @param team
     *            the given team
     * @return the turnPoints property of the given team
     */
    public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
        return turnPoints.get(team);
    }

    /**
     * Enables to modify the value contained by the turnPoints property of the
     * given team.
     * 
     * @param team
     *            the given team
     * @param newTurnPoints
     *            the new value of the property
     */
    public void setTurnPoints(TeamId team, int newTurnPoints) {
        turnPoints.get(team).setValue(newTurnPoints);
    }

    /**
     * Returns the gamePoints property of the given team.
     * 
     * @param team
     *            the given team
     * @return the gamePoints property of the given team
     */
    public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
        return gamePoints.get(team);
    }

    /**
     * Enable to modify the value contained by the gamePoints property of the
     * given team.
     * 
     * @param team
     *            the given team
     * @param newGamePoints
     *            the new value of the property
     */
    public void setGamePoints(TeamId team, int newGamePoints) {
        gamePoints.get(team).setValue(newGamePoints);
    }

    /**
     * Returns the totalPoints property of the given team.
     * 
     * @param team
     *            the given team
     * @return the totalPoints property of the given team
     */
    public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
        return totalPoints.get(team);
    }

    /**
     * Enables to modify the value contained by the totalPoints property of the
     * given team.
     * 
     * @param team
     *            the given team
     * @param newTotalPoints
     *            the new value of the property
     */
    public void setTotalPoints(TeamId team, int newTotalPoints) {
        totalPoints.get(team).setValue(newTotalPoints);
    }

    /**
     * Returns the winningTeam property.
     * 
     * @return the winningTeam property
     */
    public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
        return winningTeam;
    }

    /**
     * Enables to modify the value contained by the winningTeam property.
     * 
     * @param newWinningTeam
     *            the new value of the property
     */
    public void setWinningTeam(TeamId newWinningTeam) {
        winningTeam.setValue(newWinningTeam);
    }
}