package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.PackedScore;

/**
 * Represents the scores of a Jass game.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class Score {

    /**
     * Represent the initial score of a game.
     */
    public static final Score INITIAL = new Score(PackedScore.INITIAL);

    private final long packedScore;

    private Score(long packedScore) {
        this.packedScore = packedScore;
    }

    /**
     * Gives the Score object of the given packed score.
     * 
     * @param packed
     *            : the packed score
     * @return returns the Score object of the given packed score
     * @throws IllegalArgumentException
     *             if this argument does not represent a valid packed score
     *             according to PackedScore's isValid method.
     */
    public static Score ofPacked(long packed) throws IllegalArgumentException {
        Preconditions.checkArgument(PackedScore.isValid(packed));
        return new Score(packed);
    }

    /**
     * Gives the packed version of the Score.
     * 
     * @return returns the packed version of the Score
     */
    public long packed() {
        return packedScore;
    }

    /**
     * Gives the number of folds won by the given team in the current turn.
     * 
     * @param t
     *            : the given team
     * @return returns the number of folds won by the given team in the current
     *         turn
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(packedScore, t);
    }

    /**
     * Gives the number of points won by the given team in the current turn.
     * 
     * @param t
     *            : the given team
     * @return returns the number of points won by the given team in the current
     *         turn
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(packedScore, t);
    }

    /**
     * Gives the number of points won by the given team in the previous rounds
     * (without including the current trick).
     * 
     * @param t
     *            : the given team
     * @return returns the number of points won by the given team in the
     *         previous rounds (without including the current trick)
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(packedScore, t);
    }

    /**
     * Gives the total number of points won by the given team in the game.
     * 
     * @param t
     *            : the given team
     * @return returns the total number of points won by the given team in the
     *         game
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(packedScore, t);
    }

    /**
     * Gives a Score containing the score informations updated assuming that the
     * winningTeam has won the given tricksPoints. It also attributes the 100
     * points bonus if a team has won all the tricks.
     * 
     * @param winningTeam
     *            : the winningTeam
     * @param trickPoints
     *            : the given tricksPoints
     * @return returns a Score containing the score informations updated
     *         assuming that the winningTeam has won the given tricksPoints. It
     *         also attributes the 100 points bonus if a team has won all the
     *         tricks.
     * @throws IllegalArgumentException
     *             if the given number of points is less than 0
     */
    public Score withAdditionalTrick(TeamId winningTeam, int trickPoints)
            throws IllegalArgumentException {
        Preconditions.checkArgument(trickPoints >= 0);
        return new Score(PackedScore.withAdditionalTrick(packedScore,
                winningTeam, trickPoints));
    }

    /**
     * Gives a packed long containing the score informations updated for the
     * next turn.
     * 
     * @return returns a packed long containing the score informations updated
     *         for the next turn.
     */
    public Score nextTurn() {
        return new Score(PackedScore.nextTurn(packedScore));
    }

    @Override
    public boolean equals(Object that0) {
        if (!(that0 instanceof Score)) {
            return false;
        }
        return ((Score) that0).packed() == packedScore;
    }

    @Override
    public int hashCode() {
        return ((Long) packedScore).hashCode();
    }

    @Override
    public String toString() {
        return PackedScore.toString(packedScore);
    }

}