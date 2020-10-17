package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;

/**
 * Provides methods to manipulate scores of a Jass game, packed in long
 * variables.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class PackedScore {
    private PackedScore() {
    }

    /**
     * Initial value of a score
     */
    public static final long INITIAL = 0L;

    private static final int TURNTRICKS_START = 0;
    private static final int TURNTRICK_SIZE = 4;
    private static final int TURNPOINTS_START = 4;
    private static final int TURNPOINTS_SIZE = 9;
    private static final int GAMEPOINTS_START = 13;
    private static final int GAMEPOINTS_SIZE = 11;
    private static final int OTHER_START = 24;
    private static final int OTHER_SIZE = 8;

    private static final int TURNTRICKS_MAX = Jass.TRICKS_PER_TURN;
    private static final int TURNPOINTS_MAX = 257;
    private static final int GAMEPOINTS_MAX = 2000;

    /**
     * Checks if the given long represents a valid score.
     * 
     * @param pkScore
     *            the score to check
     * @return true if pkScore represents a valid score, and false otherwise
     */
    public static boolean isValid(long pkScore) {
        for (int i = 0; i < Long.SIZE; i += Integer.SIZE) {
            int turnTricks = (int) Bits64.extract(pkScore, TURNTRICKS_START + i,
                    TURNTRICK_SIZE);
            int turnPoints = (int) Bits64.extract(pkScore, TURNPOINTS_START + i,
                    TURNPOINTS_SIZE);
            int gamePoints = (int) Bits64.extract(pkScore, GAMEPOINTS_START + i,
                    GAMEPOINTS_SIZE);
            int blank = (int) Bits64.extract(pkScore, OTHER_START + i,
                    OTHER_SIZE);
            if (turnTricks > TURNTRICKS_MAX || turnPoints > TURNPOINTS_MAX
                    || gamePoints > GAMEPOINTS_MAX || blank != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Packs all the given integers, informations of the score, in a long
     * variable.
     * 
     * @param turnTricks1
     *            the number of tricks won by the first team during the turn
     * @param turnPoints1
     *            the number of points won by the first team during the turn
     * @param gamePoints1
     *            the number of points won by the first team during the game
     * @param turnTricks2
     *            the number of tricks won by the second team during the turn
     * @param turnPoints2
     *            the number of points won by the second team during the turn
     * @param gamePoints2
     *            the number of points won by the second team during the game
     * @return the packed long
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1,
            int turnTricks2, int turnPoints2, int gamePoints2) {
        int[][] scoreParts = { { turnTricks1, turnPoints1, gamePoints1 },
                { turnTricks2, turnPoints2, gamePoints2 } };
        int[] packed = new int[TeamId.COUNT];
        for (int i = 0; i < packed.length; i++) {
            packed[i] = Bits32.pack(scoreParts[i][0], TURNTRICK_SIZE,
                    scoreParts[i][1], TURNPOINTS_SIZE, scoreParts[i][2],
                    GAMEPOINTS_SIZE);
        }
        return packScoresOfTwoTeams(packed[0], packed[1]);
    }

    /**
     * Returns the number of tricks won by the given team during the turn.
     * 
     * @param pkScore
     *            the packed long containing the score informations
     * @param t
     *            the team to have the trick count of during the turn
     * @return the number of tricks won by the given team during the turn
     */
    public static int turnTricks(long pkScore, TeamId t) {
        assert (isValid(pkScore));
        return (int) Bits64.extract(pkScore, Integer.SIZE * t.ordinal(),
                TURNTRICK_SIZE);
    }

    /**
     * Returns the number of points won by the given team during the turn.
     * 
     * @param pkScore
     *            the packed long containing the score informations
     * @param t
     *            the team to have the points count of during the turn
     * @return the number of points won by the given team during the turn
     */
    public static int turnPoints(long pkScore, TeamId t) {
        assert (isValid(pkScore));
        return (int) Bits64.extract(pkScore,
                TURNPOINTS_START + Integer.SIZE * t.ordinal(), TURNPOINTS_SIZE);
    }

    /**
     * Returns the number of points won by the given team during the game. It
     * does not include the points of the current turn.
     * 
     * @param pkScore
     *            the packed long containing the score informations
     * @param t
     *            the team to have the points count of during the game
     * @return the number of points won by the given team during the game,
     *         excluding the current turn
     */
    public static int gamePoints(long pkScore, TeamId t) {
        assert (isValid(pkScore));
        return (int) Bits64.extract(pkScore,
                GAMEPOINTS_START + Integer.SIZE * t.ordinal(), GAMEPOINTS_SIZE);
    }

    /**
     * Returns the number of points won by the given team during the game,
     * including the current turn.
     * 
     * @param pkScore
     *            the packed long containing the score informations
     * @param t
     *            the team to have the points count of during the game
     * @return the number of points won by the given team during the game,
     *         including the current turn
     */
    public static int totalPoints(long pkScore, TeamId t) {
        return gamePoints(pkScore, t) + turnPoints(pkScore, t);
    }

    /**
     * Returns a long containing the score informations updated assuming that
     * the winningTeam has won the given tricksPoints. It also attributes the
     * 100 points bonus if a team has won all the tricks.
     * 
     * @param pkScore
     *            the packed long containing the score informations
     * @param winningTeam
     *            the team to add points to
     * @param trickPoints
     *            the points won with sthe current trick
     * @return the packed long, containing the updated score
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam,
            int trickPoints) {
        int newTurnTricks = turnTricks(pkScore, winningTeam) + 1;
        int newTurnPoints = turnPoints(pkScore, winningTeam) + trickPoints;
        if (newTurnTricks == Jass.TRICKS_PER_TURN) {
            newTurnPoints += Jass.MATCH_ADDITIONAL_POINTS;
        }

        return updateScores(pkScore, winningTeam.ordinal(), newTurnTricks,
                newTurnPoints);
    }

    /**
     * Returns a packed long containing the score informations updated for the
     * next turn.
     * 
     * @param pkScore
     *            the packed long containing the score informations
     * @return the packed long, containing the updated score
     */
    public static long nextTurn(long pkScore) {
        return pack(0, 0, totalPoints(pkScore, TeamId.TEAM_1), 0, 0,
                totalPoints(pkScore, TeamId.TEAM_2));
    }

    /**
     * Returns a textual representation of the score.
     * 
     * @param pkScore
     *            the packed long, containing the updated score
     * @return a String of the form
     *         "(turnTricks1,turnPoints1,gamePoints1)/(turnTricks2,turnPoints2,gamePoints2)"
     */
    public static String toString(long pkScore) {
        return teamToString(pkScore, TeamId.TEAM_1) + "/"
                + teamToString(pkScore, TeamId.TEAM_2);
    }

    /*
     * Provides the new score according to the old score, the number of the
     * winning team, the number of tricks won by the given team during the turn,
     * and the number of points won by the given team during the turn.
     */
    private static long updateScores(long pkScore, int teamOrdinal, int newTurnTricks,
            int newTurnPoints) {
        int[] pkScores = { (int) Bits64.extract(pkScore, 0, Integer.SIZE),
                (int) Bits64.extract(pkScore, Integer.SIZE, Integer.SIZE) };
        pkScores[teamOrdinal] &= ~(Bits32.mask(0, GAMEPOINTS_START));
        pkScores[teamOrdinal] |= Bits32.pack(newTurnTricks, TURNTRICK_SIZE, newTurnPoints,
                TURNPOINTS_SIZE);
        return packScoresOfTwoTeams(pkScores[0], pkScores[1]);
    }

    // Packs the score of two teams into one long (packed Score).
    private static long packScoresOfTwoTeams(int pkScore1, int pkScore2) {
        return Bits64.pack(pkScore1, Integer.SIZE, pkScore2, Integer.SIZE);
    }

    // Returns the string representation of the score of the given team.
    private static String teamToString(long pkScore, TeamId t) {
        return "(" + turnTricks(pkScore, t) + "," + turnPoints(pkScore, t) + ","
                + gamePoints(pkScore, t) + ")";
    }
}