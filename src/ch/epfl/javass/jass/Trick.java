package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * Represents a trick.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class Trick {
    /**
     * Represents an invalid trick.
     */
    public final static Trick INVALID = new Trick(PackedTrick.INVALID);

    private final int pkTrick;

    private Trick(int pkTrick) {
        this.pkTrick = pkTrick;
    }

    /**
     * Returns the empty Trick containing the given player and the trump suit.
     * 
     * @param trump
     *            the trump suit
     * @param firstPlayer
     *            the first player
     * @return the empty Trick containing the trump suit and the first player
     */
    public static Trick firstEmpty(Color trump, PlayerId firstPlayer) {
        return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * Returns a Trick corresponding to the packed trick given.
     * 
     * @param packed
     *            the packed trick
     * @return the Trick corresponding to packed
     * @throws IllegalArgumentException
     *             if packed does not represent a valid trick
     */
    public static Trick ofPacked(int packed) throws IllegalArgumentException {
        Preconditions.checkArgument(PackedTrick.isValid(packed));
        return new Trick(packed);
    }

    /**
     * Returns the packed trick corresponding to the current Trick.
     * 
     * @return the packed trick of the current Trick
     */
    public int packed() {
        return pkTrick;
    }

    /**
     * Returns the empty Trick with the same trump, the winning player and the
     * next trick index of the current Trick. Returns the invalid Trick if the
     * given current Trick is the last one of the turn (index 8).
     * 
     * @return the empty corresponding Trick
     * @throws IllegalStateException
     *             if the Trick is not full
     */
    public Trick nextEmpty() throws IllegalStateException {
        if (!PackedTrick.isFull(pkTrick)) {
            throw new IllegalStateException(
                    "Trick must be empty to create a new one");
        }
        return new Trick(PackedTrick.nextEmpty(packed()));
    }

    /**
     * Returns true if the current Trick is empty (i.e. does not contain any
     * card).
     * 
     * @return true if the trick is empty
     */
    public boolean isEmpty() {
        return PackedTrick.isEmpty(pkTrick);
    }

    /**
     * Returns true if the current Trick is full (i.e. does contain four cards).
     * 
     * @return true if the trick is full
     */
    public boolean isFull() {
        return PackedTrick.isFull(pkTrick);
    }

    /**
     * Returns true iff the Trick index is the last one (i.e. it is 8).
     * 
     * @return true if the current Trick is the last trick of the turn, false
     *         otherwise
     */
    public boolean isLast() {
        return PackedTrick.isLast(pkTrick);
    }

    /**
     * Returns the Trick size (i.e. the number of cards it contains).
     * 
     * @return the size of the current Trick
     */
    public int size() {
        return PackedTrick.size(pkTrick);
    }

    /**
     * Returns the trump suit of the current Trick.
     * 
     * @return the trump suit of the current Trick
     */
    public Color trump() {
        return PackedTrick.trump(pkTrick);
    }

    /**
     * Returns the index of the current Trick.
     * 
     * @return the index of the current Trick
     */
    public int index() {
        return PackedTrick.index(pkTrick);
    }

    /**
     * Returns the player at the given index, assuming that the player at index
     * 0 is the first player of the trick.
     * 
     * @param index
     *            the index of the player of the current Trick to extract
     * @return the player who plays the index's card in the current Trick
     * @throws IndexOutOfBoundsException
     *             if the index does not point an existing player
     */
    public PlayerId player(int index) throws IndexOutOfBoundsException {
        Preconditions.checkIndex(index, PlayerId.COUNT);
        return PackedTrick.player(pkTrick, index);
    }

    /**
     * Returns the Card at the given index of the trick.
     * 
     * @param index
     *            the index of the Card to extract
     * @return the index's position Card of the current trick
     * @throws IndexOutOfBoundsException
     *             if the index does not point an existing card
     */
    public Card card(int index) throws IndexOutOfBoundsException {
        Preconditions.checkIndex(index, size());
        return Card.ofPacked(PackedTrick.card(pkTrick, index));
    }

    /**
     * Returns an updated Trick updated with the given Card.
     * 
     * @param c
     *            the card to add
     * @return the updated Trick
     * @throws if
     *             the Trick is full
     */
    public Trick withAddedCard(Card c) throws IllegalStateException {
        if (isFull()) {
            throw new IllegalStateException(
                    "The trick must not be full to be able to add a card");
        }
        return new Trick(PackedTrick.withAddedCard(pkTrick, c.packed()));
    }

    /**
     * Returns the base color of the current Trick (i.e. the color of the first
     * card played).
     * 
     * @return the base color of the current Trick
     * @throws IllegalStateException
     *             if the Trick is empty
     */
    public Color baseColor() throws IllegalStateException {
        if (isEmpty()) {
            throw new IllegalStateException(
                    "The trick must not be empty to be able to identifie the base color");
        }
        return PackedTrick.baseColor(pkTrick);
    }

    /**
     * Returns a all the playable card contained in hand according to the card
     * already played in the current Trick.
     * 
     * @param hand
     *            the cards of the player to play
     * @return a card set containing all the playable card of hand
     * @throws IllegalStateException
     *             if the Trick is full
     */
    public CardSet playableCards(CardSet hand) throws IllegalStateException {
        if (isFull()) {
            throw new IllegalStateException(
                    "The trick must not be full to be able to list the playable cards");
        }
        return CardSet
                .ofPacked(PackedTrick.playableCards(pkTrick, hand.packed()));
    }

    /**
     * Returns the total value of the current Trick.
     * 
     * @return the points of the current Trick
     */
    public int points() {
        return PackedTrick.points(pkTrick);
    }

    /**
     * Returns the player leading the current Trick.
     * 
     * @return the leading player of the current Trick
     * @throws IllegalStateException
     *             if the Trick is empty
     */
    public PlayerId winningPlayer() throws IllegalStateException {
        if (isEmpty()) {
            throw new IllegalStateException(
                    "The trick must not be empty to be able to identifie a winning player");
        }
        return PackedTrick.winningPlayer(pkTrick);
    }

    @Override
    public boolean equals(Object that0) {
        if (!(that0 instanceof Trick)) {
            return false;
        }
        return ((Trick) that0).packed() == pkTrick;
    }

    @Override
    public int hashCode() {
        return pkTrick;
    }

    @Override
    public String toString() {
        return PackedTrick.toString(pkTrick);
    }

}