package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;

/**
 * Contains static methods allowing manipulation of cards of a Jass game, each
 * represented as an integer.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class PackedCard {
    private static final int RANK_START = 0;
    private static final int RANK_SIZE = 4;
    private static final int COLOR_START = 4;
    private static final int COLOR_SIZE = 2;
    private static final int OTHER_START = 6;
    private static final int OTHER_SIZE = 26;

    private final static int[] trumpPoints = { 0, 0, 0, 14, 10, 20, 3, 4, 11 };
    private final static int[] basicPoints = { 0, 0, 0, 0, 10, 2, 3, 4, 11 };

    
    private PackedCard() {
    }

    /**
     * Represents an invalid packed card.
     */
    public final static int INVALID = 0b111111;

    /**
     * Tells if the given value is a valid packed card.
     * 
     * @param pkCard
     *            : the value of the packed card
     * @return returns true if the given value is a valid packed card, i.e. if
     *         the bits containing the rank contain a value between 0 and 8
     *         (inclusive) and the unused bits are all 0
     */
    public static boolean isValid(int pkCard) {
        int rank = Bits32.extract(pkCard, RANK_START, RANK_SIZE);
        int blank = Bits32.extract(pkCard, OTHER_START, OTHER_SIZE);
        return rank < Card.Rank.COUNT && blank == 0;
    }

    /**
     * Gives the packed card of the given color and rank.
     * 
     * @param c
     *            : the given color
     * @param r
     *            : the given rank
     * @return returns the packed card of given color and rank
     */
    public static int pack(Card.Color c, Card.Rank r) {
        int color = c.ordinal();
        int rank = r.ordinal();
        return Bits32.pack(rank, RANK_SIZE, color, COLOR_SIZE);
    }

    /**
     * Gives the color of the given packed card.
     * 
     * @param pkCard
     *            : the given packed card
     * @return returns the color of the given packed card
     */
    public static Card.Color color(int pkCard) {
        assert (isValid(pkCard));
        int color = Bits32.extract(pkCard, COLOR_START, COLOR_SIZE);
        return Card.Color.values()[color];
    }

    /**
     * Gives the rank of the given packed card.
     * 
     * @param pkCard
     *            : the given packed card
     * @return returns the rank of the given packed card
     */
    public static Card.Rank rank(int pkCard) {
        assert (isValid(pkCard));
        int rank = Bits32.extract(pkCard, RANK_START, RANK_SIZE);
        return Card.Rank.values()[rank];
    }

    /**
     * Tells if the first card given is greater than the second, according to
     * the trump suit given.
     * 
     * @param trump
     *            : the trump suit
     * @param pkCardL
     *            : the first given card
     * @param pkCardR
     *            : the second given card
     * @return returns true if the first card given is greater than the second,
     *         according to the trump suit given
     * 
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        if (color(pkCardL).equals(trump)) {
            if (color(pkCardR).equals(trump)) {
                return rank(pkCardL).trumpOrdinal() > rank(pkCardR)
                        .trumpOrdinal();
            } else {
                return true;
            }
        } else if (color(pkCardL).equals(color(pkCardR))) {
            return rank(pkCardL).ordinal() > rank(pkCardR).ordinal();
        } else {
            return false;
        }

    }

    /**
     * Gives the value of the given packed card, according to the trump suit
     * given.
     * 
     * @param trump
     *            : the trump suit
     * @param pkCard
     *            : the given packed card
     * @return returns the value of the given packed card, according to the
     *         trump suit given
     */
    public static int points(Card.Color trump, int pkCard) {
        Card.Rank rank = rank(pkCard);
        if (color(pkCard).equals(trump)) {
            return trumpPoints[rank.ordinal()];
        } else {
            return basicPoints[rank.ordinal()];
        }
    }

    /**
     * Gives a representation of the packed card given as a string consisting of
     * the color symbol and the short for the rank name.
     * 
     * @param pkCard
     *            : the given packed card
     * @return returns a representation of the packed card given as a string
     *         consisting of the color symbol and the short for the rank name
     */
    public static String toString(int pkCard) {
        return color(pkCard).toString() + rank(pkCard).toString();
    }
}