package ch.epfl.javass.jass;

import static ch.epfl.javass.bits.Bits64.mask;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;

/**
 * Enables to manipulate deck of cards packed into long type values.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class PackedCardSet {
    /**
     * The packed empty card set.
     */
    public static final long EMPTY = 0L;

    /**
     * The packed card set of the 36 cards of the game.
     */
    public static final long ALL_CARDS = generateAllCards();

    private static long generateAllCards() {
        long allCards = EMPTY;
        int sameSuitCardSize = Long.SIZE / Card.Color.COUNT;
        for (int i = 0; i < Card.Color.COUNT; i++) {
            allCards |= Bits64.mask(sameSuitCardSize * i, Card.Rank.COUNT);
        }
        return allCards;
    }

    private static final int RANK_START = 0;
    private static final int RANK_SIZE = 4;
    private static final int COLOR_START = 4;
    private static final int COLOR_SIZE = 2;

    private static final long[][] TRUMP_ABOVE = computeTrumpAbove();

    private static final long[] SUBSET_OF_COLOR = new long[] { mask(0, Card.Rank.COUNT),
            mask(16, Card.Rank.COUNT), mask(32, Card.Rank.COUNT), mask(48, Card.Rank.COUNT) };

    // Computes the TRUMP_ABOVE array in a general way.
    private static long[][] computeTrumpAbove() {
        long[][] trumpAbove = new long[Card.Color.COUNT][Card.Rank.COUNT];
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rankToAdd : Card.Rank.ALL) {
                for (Card.Rank rankToCompare : Card.Rank.ALL) {
                    // If the first card is [trump] better than the second card,
                    // we add it to cardSet of all the cards [trump] better than
                    // the second card.
                    if (rankToAdd.trumpOrdinal() > rankToCompare
                            .trumpOrdinal()) {
                        trumpAbove[color.ordinal()][rankToCompare
                                .ordinal()] = add(
                                        trumpAbove[color
                                                .ordinal()][rankToCompare
                                                        .ordinal()],
                                        PackedCard.pack(color, rankToAdd));
                    }
                }
            }
        }
        return trumpAbove;
    }

    private PackedCardSet() {
    }

    /**
     * Tells whether the given value represents a valid packed card set or not.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @return true if the given value represents a valid packed card set, i.e.
     *         if none of the 28 unused bits is 1
     */
    public static boolean isValid(long pkCardSet) {
        return (pkCardSet | ALL_CARDS) == ALL_CARDS;
    }

    /**
     * Gives the card set of strictly stronger cards than the trump packed card
     * given
     * 
     * @param pkCard
     *            : the trump packed card given
     * @return the card set of strictly stronger cards than the trump packed
     *         card given
     */
    public static long trumpAbove(int pkCard) {
        assert (PackedCard.isValid(pkCard));

        int color = PackedCard.color(pkCard).ordinal();
        int rank = PackedCard.rank(pkCard).ordinal();
        return TRUMP_ABOVE[color][rank];
    }

    /**
     * Gives the packed card set containing only the packed card given.
     * 
     * @param pkCard
     *            : the packed card given
     * @return the packed card set containing only the packed card given
     */
    public static long singleton(int pkCard) {
        assert (PackedCard.isValid(pkCard));

        return Bits64.mask(pkCard, 1);
    }

    /**
     * Tells if the packed card set is empty
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @return true if the given packed set of cards is empty
     */
    public static boolean isEmpty(long pkCardSet) {
        assert (isValid(pkCardSet));

        return pkCardSet == EMPTY;
    }

    /**
     * Gives the size of the given packed card set.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @return the size of the given packed card set (the number of cards it
     *         contains)
     */
    public static int size(long pkCardSet) {
        assert (isValid(pkCardSet));

        return Long.bitCount(pkCardSet);
    }

    /**
     * Gives the packed version of the card at the given index in the given card
     * set.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @param index
     *            : the given index
     * @return the packed version of the card at the given index in the given
     *         card set (the index 0 corresponds to the least significant bit
     *         equal to 1)
     */
    public static int get(long pkCardSet, int index) {
        assert (index < size(pkCardSet));

        for (int i = 0; i < index; i++) {
            pkCardSet = difference(pkCardSet, Long.lowestOneBit(pkCardSet));
        }
        return Long.numberOfTrailingZeros(pkCardSet);
    }

    /**
     * Gives the packed card set obtained by adding the given card to the given
     * packed card set.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @param pkCard
     *            : the given packed card
     * @return the given packed card set to which the given packed card was
     *         added
     */
    public static long add(long pkCardSet, int pkCard) {
        return union(singleton(pkCard), pkCardSet);
    }

    /**
     * Gives the packed card set obtained by removing the given card from the
     * given packed card set.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @param pkCard
     *            : the given packed card
     * @return the given packed card set from which the given packed card was
     *         removed
     */
    public static long remove(long pkCardSet, int pkCard) {
        return difference(pkCardSet, singleton(pkCard));
    }

    /**
     * Tells if the given packed card set contains the given packed card.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @param pkCard
     *            : the given packed card
     * @return true if the given packed card set contains the given packed card
     */
    public static boolean contains(long pkCardSet, int pkCard) {
        return !isEmpty(intersection(singleton(pkCard), pkCardSet));
    }

    /**
     * Gives the complement of the given packed card set.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @return the complement of the given packed card set (the card set of all
     *         the cards not contained in the given card set)
     */
    public static long complement(long pkCardSet) {
        assert (isValid(pkCardSet));

        return (ALL_CARDS ^ pkCardSet);
    }

    /**
     * Gives the union of the two packed card sets.
     * 
     * @param pkCardSet1
     *            : the first packed card set
     * @param pkCardSet2
     *            : the second packed card set
     * @return the union of the two packed card sets
     */
    public static long union(long pkCardSet1, long pkCardSet2) {
        assert (isValid(pkCardSet1));
        assert (isValid(pkCardSet2));

        return pkCardSet1 | pkCardSet2;
    }

    /**
     * Gives the intersection of the two packed card sets.
     * 
     * @param pkCardSet1
     *            : the first packed card set
     * @param pkCardSet2
     *            : the second given packed card set
     * @return the intersection of the two packed card sets
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
        assert (isValid(pkCardSet1));
        assert (isValid(pkCardSet2));

        return pkCardSet1 & pkCardSet2;
    }

    /**
     * Gives the difference of the two packed card sets.
     * 
     * @param pkCardSet1
     *            : the first packed card set
     * @param pkCardSet2
     *            : the second packed card set
     * @return the difference of the two packed card sets
     */
    public static long difference(long pkCardSet1, long pkCardSet2) {
        return pkCardSet1 - intersection(pkCardSet1, pkCardSet2);
    }

    /**
     * Gives the subset of the given packed card set consisting only of cards of
     * the given color.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @param color
     *            : the given color
     * @return the subset of the given packed card set consisting only of cards
     *         of the given color
     */
    public static long subsetOfColor(long pkCardSet, Card.Color color) {
        return intersection(pkCardSet, SUBSET_OF_COLOR[color.ordinal()]);
    }

    /**
     * Gives the text representation of the given packed card set.
     * 
     * @param pkCardSet
     *            : the given packed card set
     * @return the text representation of the given packed card set
     */
    public static String toString(long pkCardSet) {
        assert (isValid(pkCardSet));

        StringJoiner j = new StringJoiner(",", "{", "}");

        for (int i = 0; i < size(pkCardSet); i++) {
            j.add(PackedCard.toString(get(pkCardSet, i)));
        }

        return j.toString();
    }

}