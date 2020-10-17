package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

/**
 * Represents the state of a turn.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class TurnState {

    private final long pkScore;
    private final long pkUnplayedCards;
    private final int pkTrick;

    private TurnState(long pkScore, long pkUnplayedCards, int pkTrick) {
        this.pkScore = pkScore;
        this.pkUnplayedCards = pkUnplayedCards;
        this.pkTrick = pkTrick;
    }

    /**
     * Gives the initial state corresponding to a turnState whose trump, the
     * initial score and the initial player are those given.
     * 
     * @param trump
     *            : the given trump
     * @param score
     *            : the given initial score
     * @param firstPlayer
     *            : the given initial player
     * @return the initial state corresponding to the turnState
     */
    public static TurnState initial(Card.Color trump, Score score,
            PlayerId firstPlayer) {
        int pkTrick = PackedTrick.firstEmpty(trump, firstPlayer);
        return new TurnState(score.packed(), PackedCardSet.ALL_CARDS, pkTrick);
    }

    /**
     * Returns the state whose (packed) components are those given.
     * 
     * @param pkScore
     *            : the given packed score
     * @param pkUnplayedCards
     *            : the given packed unplayed cards
     * @param pkTrick
     *            : the given packed trick
     * @return the TurnState
     */
    public static TurnState ofPackedComponents(long pkScore,
            long pkUnplayedCards, int pkTrick) {
        Preconditions.checkArgument(PackedScore.isValid(pkScore));
        Preconditions.checkArgument(PackedCardSet.isValid(pkUnplayedCards));
        Preconditions.checkArgument(PackedTrick.isValid(pkTrick));
        return new TurnState(pkScore, pkUnplayedCards, pkTrick);
    }

    /**
     * Getter of the packed score.
     * 
     * @return the packed version of the current score
     */
    public long packedScore() {
        return pkScore;
    }

    /**
     * Getter of the packed unplayed cards.
     * 
     * @return the packed version of the unplayed cards
     */
    public long packedUnplayedCards() {
        return pkUnplayedCards;
    }

    /**
     * Getter of the packed current trick.
     * 
     * @return the packed version of the current trick
     */
    public int packedTrick() {
        return pkTrick;
    }

    /**
     * Getter of the current score as an object.
     * 
     * @return the current score
     */
    public Score score() {
        return Score.ofPacked(pkScore);
    }

    /**
     * Getter of the unplayed cards as an object.
     * 
     * @return the unplayed cards
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(pkUnplayedCards);
    }

    /**
     * Getter of the current trick as an object.
     * 
     * @return the current trick
     */
    public Trick trick() {
        return Trick.ofPacked(pkTrick);
    }

    /**
     * Tells if the state is terminal.
     * 
     * @return true if the state is terminal, i.e. if the last trick of the turn
     *         has been played
     */
    public boolean isTerminal() {
        return pkTrick == PackedTrick.INVALID;
    }

    /**
     * Gives the identity of the player to play the next card.
     * 
     * @return the identity of the player to play the next card
     */
    public PlayerId nextPlayer() {
        checkTrickNotFull();
        return PackedTrick.player(pkTrick, PackedTrick.size(pkTrick));
    }

    /**
     * Gives the state corresponding to the one applied after the next player
     * played the given card.
     * 
     * @param card
     *            : the given played card
     * @return the state corresponding to the one applied after the next player
     *         played the given card
     */
    public TurnState withNewCardPlayed(Card card) {
        checkTrickNotFull();
//        Preconditions.checkArgument(PackedCardSet.contains(pkUnplayedCards, card.packed()));
        long newPkUnplayedCards = PackedCardSet.remove(pkUnplayedCards,
                card.packed());
        int newPkTrick = PackedTrick.withAddedCard(pkTrick, card.packed());
        return new TurnState(pkScore, newPkUnplayedCards, newPkTrick);
    }

    /**
     * Gives the state corresponding to the one that is applied after the
     * current trick has been picked up.
     * 
     * @return the state corresponding to the one that is applied after the
     *         current trick has been picked up
     */
    public TurnState withTrickCollected() {
        if (!PackedTrick.isFull(pkTrick)) {
            throw new IllegalStateException("Trick is supposed to be full");
        }

        long newPkScore = PackedScore.withAdditionalTrick(pkScore,
                PackedTrick.winningPlayer(pkTrick).team(),
                PackedTrick.points(pkTrick));
        int newPkTrick = PackedTrick.nextEmpty(pkTrick);
        return new TurnState(newPkScore, pkUnplayedCards, newPkTrick);
    }

    /**
     * Gives the state corresponding to the one to which it is applied after the
     * next player has played the given card, and that the current trick has
     * been picked up if it is then full.
     * 
     * @param card
     *            : the given played card
     * @return the state corresponding to the one to which it is applied after
     *         the next player has played the given card, and that the current
     *         trick has been picked up if it is then full
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
        checkTrickNotFull();
        TurnState newTurnState = withNewCardPlayed(card);
        return PackedTrick.isFull(newTurnState.packedTrick())
                ? newTurnState.withTrickCollected()
                : newTurnState;
    }
    
    private void checkTrickNotFull() {
        if (PackedTrick.isFull(pkTrick)) {
            throw new IllegalStateException("Trick is not supposed to be full");
        }
    }

}