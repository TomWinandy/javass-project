package ch.epfl.javass.jass;

import java.util.List;

import ch.epfl.javass.Preconditions;

/**
 * Represents a set of cards.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class CardSet {

    /**
     * The empty set of cards.
     */
    public static final CardSet EMPTY = new CardSet(PackedCardSet.EMPTY);

    /**
     * The set of the 36 cards of the Jass game.
     */
    public static final CardSet ALL_CARDS = new CardSet(
            PackedCardSet.ALL_CARDS);

    private final long packedCardSet;

    private CardSet(long packedCardSet) {
        this.packedCardSet = packedCardSet;
    }

    /**
     * Gives the set of cards containing all the cards contained in the given
     * list.
     * 
     * @param cards
     *            : the given list of cards
     * @return the set of cards containing all the cards contained in the given
     *         list
     */
    public static CardSet of(List<Card> cards) {
        long packed = PackedCardSet.EMPTY;
        for (Card c : cards) {
            packed = PackedCardSet.add(packed, c.packed());
        }
        return new CardSet(packed);
    }

    /**
     * Gives the set of cards whose packed is the packed version.
     * 
     * @param packed
     *            : the packed version of the set of cards
     * @return the set of cards whose packed is the packed version
     */
    public static CardSet ofPacked(long packed) {
        Preconditions.checkArgument(PackedCardSet.isValid(packed));
        return new CardSet(packed);
    }

    /**
     * Gives the packed version of the set of cards.
     * 
     * @return the packed version of the set of cards
     */
    public long packed() {
        return packedCardSet;
    }

    /**
     * Tells if the set of cards is empty.
     * 
     * @return true if the set of cards is empty
     */
    public boolean isEmpty() {
        return PackedCardSet.isEmpty(packedCardSet);
    }

    /**
     * Gives the size of the set of cards.
     * 
     * @return the size of the set of cards, that is, the number of cards it
     *         contains
     */
    public int size() {
        return PackedCardSet.size(packedCardSet);
    }

    /**
     * Gives the given index card of the card set.
     * 
     * @param index
     *            : the given index card
     * @return the given index card of the card set
     */
    public Card get(int index) {
        Preconditions.checkIndex(index, size());
        return Card.ofPacked(PackedCardSet.get(packedCardSet, index));
    }

    /**
     * Gives the set of cards to which the given card was added.
     * 
     * @param card
     *            : the given card
     * @return the set of cards to which the given card was added
     */
    public CardSet add(Card card) {
        return new CardSet(PackedCardSet.add(packedCardSet, card.packed()));
    }

    /**
     * Gives the set of cards from which the given card was removed.
     * 
     * @param card
     *            : the given card
     * @return the set of cards from which the given card was removed
     */
    public CardSet remove(Card card) {
        return new CardSet(PackedCardSet.remove(packedCardSet, card.packed()));
    }

    /**
     * Tells if the set of cards contains the given card.
     * 
     * @param card
     *            : the given card
     * @return true if the set of cards contains the given card
     */
    public boolean contains(Card card) {
        return PackedCardSet.contains(packedCardSet, card.packed());

    }

    /**
     * Gives the complement of the card set.
     * 
     * @return the complement of the card set
     */
    public CardSet complement() {
        return new CardSet(PackedCardSet.complement(packedCardSet));
    }

    /**
     * Gives the union of the current set of cards with the given set of cards.
     * 
     * @param that
     *            : the given set of cards
     * @return returns the union of the current set of cards with the given set
     *         of cards
     */
    public CardSet union(CardSet that) {
        return new CardSet(PackedCardSet.union(packedCardSet, that.packed()));
    }

    /**
     * Gives the intersection of the current set of cards with the given set of
     * cards.
     * 
     * @param that
     *            : the given set of cards
     * @return returns the intersection of the current set of cards with the
     *         given set of cards
     */
    public CardSet intersection(CardSet that) {
        return new CardSet(
                PackedCardSet.intersection(packedCardSet, that.packed()));
    }

    /**
     * Gives the difference between the current set of cards and the given set of cards.
     * 
     * @param that
     *            : the given set of cards
     * @return returns the difference between the current set of cards and the given set of cards
     */
    public CardSet difference(CardSet that) {
        return new CardSet(
                PackedCardSet.difference(packedCardSet, that.packed()));
    }

    /**
     * Gives the subset of the set of cards consisting only of cards of the
     * given color.
     * 
     * @param color
     *            : the given color
     * @return the subset of the set of cards consisting only of cards of the
     *         given color
     */
    public CardSet subsetOfColor(Card.Color color) {
        return new CardSet(PackedCardSet.subsetOfColor(packedCardSet, color));
    }

    @Override
    public boolean equals(Object that0) {
        if (that0 instanceof CardSet) {
            return ((CardSet) that0).packed() == packedCardSet;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(packedCardSet);
    }

    @Override
    public String toString() {
        return PackedCardSet.toString(packedCardSet);

    }

}