package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javass.Preconditions;

/**
 * Represents a card from 36 playing card deck.
 * 
 * @author Tom Winandy (302199)
 * @author Elior Papiernik (299399)
 */
public final class Card {
    /**
     * Represents the Color of a Card.
     * 
     * @author Elior Papiernik (299399)
     * @author Tom Winandy (302199)
     */
    public enum Color {
    SPADE("\u2660"), HEART("\u2661"), // use 2661 or 2665 to select your
                                      // favourite looking heart
    DIAMOND("\u2662"), // use 2662 or 2666 to select your favourite looking
                       // diamond
    CLUB("\u2663");

        private final String symbol;

        private Color(String s) {
            symbol = s;
        }

        /**
         * Immutable List of all values of the enumeration, in declaration
         * order.
         */
        public static final List<Color> ALL = Collections
                .unmodifiableList(Arrays.asList(values()));

        /**
         * Number of values in the enumeration.
         */
        public static final int COUNT = values().length;

        @Override
        public String toString() {
            return symbol;
        }
    }

    /**
     * Represents the Rank of a Card.
     * 
     * @author Elior Papiernik (299399)
     * @author Tom Winandy (302199)
     */
    public enum Rank {
        SIX("6", 0), SEVEN("7", 1), EIGHT("8", 2), NINE("9", 7), TEN("10",
                3), JACK("J", 8), QUEEN("Q", 4), KING("K", 5), ACE("A", 6);

        private final String value;
        private final int trumpOrdinal;

        private Rank(String s, int trumpOrdinal) {
            value = s;
            this.trumpOrdinal = trumpOrdinal;
        }

        /**
         * Immutable List of all values of the enumeration, in declaration
         * order.
         */
        public static final List<Rank> ALL = Collections
                .unmodifiableList(Arrays.asList(values()));

        /**
         * Number of values in the enumeration.
         */
        public static final int COUNT = values().length;

        /**
         * Gives the position, between 0 and 8, of the trump card having this
         * rank in the order of the trump cards.
         * 
         * @return the rank of the trump card
         */
        public int trumpOrdinal() {
            return trumpOrdinal;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private final int packedCard;

    private Card(int packedCard) {
        this.packedCard = packedCard;
    }

    /**
     * Returns the Card of Color c and Rank r.
     * 
     * @param c
     *            the Color of the Card
     * @param r
     *            the Rank of the Card
     * @return the Card of Color c and Rank r
     */
    public static Card of(Card.Color c, Card.Rank r) {
        return new Card(PackedCard.pack(c, r));
    }

    /**
     * Returns the Card of packed version given. Raises an exception if the
     * packed version given does not represent a valid Card.
     * 
     * @param packed
     *            the packed version of a Card
     * @return the Card corresponding the packed version given
     * @throws IllegalArgumentException
     *             if the packed version does not represent a valid Card
     */
    public static Card ofPacked(int packed) throws IllegalArgumentException {
        Preconditions.checkArgument(PackedCard.isValid(packed));
        return new Card(packed);
    }

    /**
     * Returns the packed version of the current Card.
     * 
     * @return the packed version of the current Card.
     */
    public int packed() {
        return packedCard;
    }

    /**
     * Returns the Color of the Card.
     * 
     * @return the Color of the Card
     */
    public Card.Color color() {
        return PackedCard.color(packedCard);
    }

    /**
     * Returns the Rank of the Card.
     * 
     * @return the Rank of the Card
     */
    public Card.Rank rank() {
        return PackedCard.rank(packedCard);
    }

    /**
     * Returns true iff the current Card is better than the given Card,
     * according to the trump suit given.
     * 
     * @param trump
     *            the trump suit given
     * @param that
     *            the Card for the current instance to be compared with
     * @return true is the current Card is better than the Card given, and false
     *         otherwise
     */
    public boolean isBetter(Card.Color trump, Card that) {
        return PackedCard.isBetter(trump, packedCard, that.packed());
    }

    /**
     * Returns the value of the Card, according to the trump suit given.
     * 
     * @param trump
     *            the trump suit given
     * @return the value of the Card according to the trump suit given
     */
    public int points(Card.Color trump) {
        return PackedCard.points(trump, packedCard);
    }

    @Override
    public boolean equals(Object that0) {
        if (!(that0 instanceof Card)) {
            return false;
        }
        return ((Card) that0).packed() == packedCard;
    }

    @Override
    public int hashCode() {
        return packedCard;
    }

    @Override
    public String toString() {
        return PackedCard.toString(packedCard);
    }
}