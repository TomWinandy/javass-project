package ch.epfl.javass.jass;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

/**
 * Provides methods allowing the manipulation of packed tricks in integer
 * values.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class PackedTrick {
    private PackedTrick() {
    }

    /**
     * Represents an invalid packed trick.
     */
    public static final int INVALID = Bits32.mask(0, Integer.SIZE);

    private static final int TRICK_SIZE_MAX = 4;

    private static final int CARD_START = 0;
    private static final int CARD_SIZE = 6;
    private static final int INDEX_START = 24;
    private static final int INDEX_SIZE = 4;
    private static final int PLAYER_START = 28;
    private static final int PLAYER_SIZE = 2;
    private static final int TRUMP_START = 30;
    private static final int TRUMP_SIZE = 2;

    /**
     * Returns true if pkTricks represents a valid integer.
     * 
     * @param pkTrick
     *            the packed trick
     * @return true if both the index is between 0 and 8, and the invalid cards
     *         are contained in the upper indexes. It returns false otherwise.
     */
    public static boolean isValid(int pkTrick) {
        if (index(pkTrick) >= Jass.TRICKS_PER_TURN) {
            return false;
        }

        boolean prevValid = false;
        for (int i = TRICK_SIZE_MAX - 1; i >= 0; i--) {
            int card = card(pkTrick, i);
            boolean cardValid = PackedCard.isValid(card);
            if (!cardValid) {
                if (card != PackedCard.INVALID || prevValid) {
                    return false;
                }
            }
            prevValid = cardValid;
        }
        return true;
    }

    /**
     * Returns the empty packed trick containing the given player and the trump
     * suit.
     * 
     * @param trump
     *            the trump suit
     * @param firstPlayer
     *            the first player
     * @return the empty packed trick containing the given player and the trump
     *         suit.
     */
    public static int firstEmpty(Color trump, PlayerId firstPlayer) {
        return makeEmptyTrick(0, firstPlayer.ordinal(), trump.ordinal());
    }

    /**
     * Returns the empty packed trick with the same trump, the winning player
     * and the next trick index of the given packed trick. Returns the invalid
     * packed trick if the given packed trick is the last one of the turn (index
     * 8).
     * 
     * @param pkTrick
     *            the previous packed trick
     * @return the empty packed trick, with the updated informations
     */
    public static int nextEmpty(int pkTrick) {
        if (isLast(pkTrick)) {
            return INVALID;
        }
        return makeEmptyTrick(index(pkTrick) + 1, winningPlayer(pkTrick).ordinal(), trump(pkTrick).ordinal());
    }
    
    private static int makeEmptyTrick(int index, int playerIndex, int trumpIndex) {
        return Bits32.pack(PackedCard.INVALID, CARD_SIZE, PackedCard.INVALID,
                CARD_SIZE, PackedCard.INVALID, CARD_SIZE, PackedCard.INVALID,
                CARD_SIZE, index, INDEX_SIZE, playerIndex, PLAYER_SIZE, trumpIndex, TRUMP_SIZE);
    }

    /**
     * Returns true iff the trick index is the last one (i.e. it is 8).
     * 
     * @param pkTrick
     *            the trick to check the index
     * @return true if pkTrick is the last trick of the turn, false otherwise
     */
    public static boolean isLast(int pkTrick) {
        assert (isValid(pkTrick));

        return Bits32.extract(pkTrick, INDEX_START,
                INDEX_SIZE) == Jass.TRICKS_PER_TURN - 1;
    }

    /**
     * Returns true if the trick is empty (i.e. does not contain any card).
     * 
     * @param pkTrick
     *            the trick to check if it is empty
     * @return true if the trick is empty, false otherwise
     */
    public static boolean isEmpty(int pkTrick) {
        assert (isValid(pkTrick));

        return size(pkTrick) == 0;
    }

    /**
     * Returns true if the trick is full (i.e. does contain four cards).
     * 
     * @param pkTrick
     *            the trick to check if it is full
     * @return true if the trick is full, false otherwise
     */
    public static boolean isFull(int pkTrick) {
        assert (isValid(pkTrick));

        return size(pkTrick) == TRICK_SIZE_MAX;
    }

    /**
     * Returns the trick size (i.e. the number of cards it contains).
     * 
     * @param pkTrick
     *            the trick to check the size of
     * @return the size of the trick packed in pkTrick
     */
    public static int size(int pkTrick) {
        assert (isValid(pkTrick));

        for (int i = 0; i < TRICK_SIZE_MAX; i++) {
            if (!PackedCard.isValid(card(pkTrick, i))) {
                return i;
            }
        }
        return TRICK_SIZE_MAX;
    }

    /**
     * Returns the trump suit of the trick.
     * 
     * @param pkTrick
     *            the trick to return the trump suit of
     * @return the trump suit of pkTrick
     */
    public static Card.Color trump(int pkTrick) {
        assert (isValid(pkTrick));

        return Card.Color.ALL
                .get(Bits32.extract(pkTrick, TRUMP_START, TRUMP_SIZE));
    }

    /**
     * Returns the player at the given index, assuming that the player at index
     * 0 is the first player of the trick.
     * 
     * @param pkTrick
     *            the packed trick from which the player at given index is
     *            extracted
     * @param index
     *            the index of the player of the trick to extract
     * @return the player who plays the index's card in pkTrick
     */
    public static PlayerId player(int pkTrick, int index) {
        assert (isValid(pkTrick));
        assert (index >= 0 && index < PlayerId.COUNT);

        return PlayerId.ALL.get(
                (Bits32.extract(pkTrick, PLAYER_START, PLAYER_SIZE) + index)
                        % PlayerId.COUNT);
    }

    /**
     * Returns the index of the trick.
     * 
     * @param pkTrick
     *            the packed trick to know the index of
     * @return the index of pkTrick
     */
    public static int index(int pkTrick) {
        return Bits32.extract(pkTrick, INDEX_START, INDEX_SIZE);
    }

    /**
     * Returns the packed version of the card at the given index of the trick.
     * 
     * @param pkTrick
     *            the packed trick to extract the card from
     * @param index
     *            the index of the card to extract
     * @return the packed version of the card in pkTrick at index's position
     */
    public static int card(int pkTrick, int index) {
        assert (index >= 0 && index < TRICK_SIZE_MAX);

        return Bits32.extract(pkTrick, CARD_START + CARD_SIZE * index,
                CARD_SIZE);
    }

    /**
     * Returns pkTrick updated with pkCard.
     * 
     * @param pkTrick
     *            the trick to update
     * @param pkCard
     *            the card to add
     * @return the updated packed trick
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        assert (PackedCard.isValid(pkCard));

        int shift = CARD_SIZE * size(pkTrick);
        int mask = ~Bits32.mask(shift, CARD_SIZE);
        int cleared = pkTrick & mask;
        return cleared | (pkCard << shift);
    }

    /**
     * Returns the base color of the trick (i.e. the color of the first card
     * played).
     * 
     * @param pkTrick
     *            the packed trick to extract the base color from
     * @return the base color of pkTrick
     */
    public static Card.Color baseColor(int pkTrick) {
        assert (isValid(pkTrick));
        assert (size(pkTrick) != 0);

        return PackedCard.color(card(pkTrick, 0));
    }

    /**
     * Returns all the playable card contained in pkHand according to the card
     * already played in pkTrick.
     * 
     * @param pkTrick
     *            the packed trick to have informations from
     * @param pkHand
     *            the cards of the player to play
     * @return a long containing all the playable card contained in pkHand, in
     *         the PackedCardSet format
     */
    public static long playableCards(int pkTrick, long pkHand) {
        assert (isValid(pkTrick));
        assert (PackedCardSet.isValid(pkHand));

        if (isEmpty(pkTrick)) {
            return pkHand;
        }

        long playableCards = 0L;
        long baseCards = PackedCardSet.subsetOfColor(pkHand,
                baseColor(pkTrick));
        long trumpCards = PackedCardSet.subsetOfColor(pkHand, trump(pkTrick));
        long nonTrumpCards = PackedCardSet.difference(pkHand, trumpCards);

        if (canFollow(pkTrick, pkHand)) {
            playableCards |= baseCards;
        } else {
            playableCards |= nonTrumpCards;
        }

        if (baseCards != trumpCards) {
            if (PackedCard.color(bestCard(pkTrick)).equals(trump(pkTrick))) {
                playableCards |= PackedCardSet.intersection(trumpCards,
                        PackedCardSet.trumpAbove(bestCard(pkTrick)));
            } else {
                playableCards |= trumpCards;
            }
        } else if (trumpCards == PackedCardSet
                .singleton(PackedCard.pack(trump(pkTrick), Rank.JACK))) {
            playableCards = pkHand;
        }

        if (PackedCardSet.isEmpty(playableCards)) {
            playableCards = pkHand;
        }
        return playableCards;
    }

    /**
     * Returns the total value of the trick.
     * 
     * @param pkTrick
     *            the packed trick to extract the points from
     * @return the points of the trick
     */
    public static int points(int pkTrick) {
        assert (isValid(pkTrick));

        int points = 0;
        Card.Color trump = trump(pkTrick);
        for (int i = 0; i < size(pkTrick); i++) {
            points += PackedCard.points(trump, card(pkTrick, i));
        }
        if (isLast(pkTrick)) {
            points += Jass.LAST_TRICK_ADDITIONAL_POINTS;
        }
        return points;
    }

    /**
     * Returns the player leading the trick.
     * 
     * @param pkTrick
     *            the packed trick to analyze
     * @return the leading player of the trick
     */
    public static PlayerId winningPlayer(int pkTrick) {
        assert (size(pkTrick) > 0);

        Card.Color trump = trump(pkTrick);
        int bestIndex = 0;
        for (int i = 1; i < size(pkTrick); i++) {
            if (PackedCard.isBetter(trump, card(pkTrick, i),
                    card(pkTrick, bestIndex))) {
                bestIndex = i;
            }
        }
        return player(pkTrick, bestIndex);
    }

    /**
     * Returns a textual representation of the played cards.
     * 
     * @param pkTrick
     *            the trick to be analyzed
     * @return the String contained the textual representation of the played
     *         cards
     */
    public static String toString(int pkTrick) {
        assert (isValid(pkTrick));

        StringJoiner j = new StringJoiner(", ");
        for (int i = 0; i < size(pkTrick); i++) {
            j.add(PackedCard.toString(card(pkTrick, i)));
        }
        return "Pli " + index(pkTrick) + " Commencé par " + player(pkTrick, 0)
                + " : " + j.toString();
    }

    // Tells if there are cards of the base suit in the given hand.
    private static boolean canFollow(int pkTrick, long pkHand) {
        return !PackedCardSet.isEmpty(
                PackedCardSet.subsetOfColor(pkHand, baseColor(pkTrick)));
    }

    // Returns the best card played in the trick.
    private static int bestCard(int pkTrick) {
        return card(pkTrick, playerIndex(pkTrick, winningPlayer(pkTrick)));
    }

    // Returns the player's index in the trick.
    private static int playerIndex(int pkTrick, PlayerId player) {
        return (player.ordinal()
                - Bits32.extract(pkTrick, PLAYER_START, PLAYER_SIZE) + 4) % 4;
    }
}