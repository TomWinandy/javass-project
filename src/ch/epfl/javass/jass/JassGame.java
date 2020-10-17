package ch.epfl.javass.jass;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a Jass game.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class JassGame {

    private final Random shuffleRng;
    private final Random trumpRng;
    private final Map<PlayerId, Player> players;
    private final Map<PlayerId, String> playerNames;
    private TurnState turnState;
    private Map<PlayerId, CardSet> cardsOfPlayers;
    private PlayerId currentFirstPlayer;
    private List<Card> ALL_CARDS = generateCards();
    private boolean gameOver;

    /**
     * Constructs a Jass game with the given random seed and players, who's
     * identity is given by the first Map and who's name is given by the second
     * map.
     * 
     * @param rngSeed
     *            the random seed
     * @param players
     *            a Map for the players' identity
     * @param playerNames
     *            a Map for the players' name
     */
    public JassGame(long rngSeed, Map<PlayerId, Player> players,
            Map<PlayerId, String> playerNames) {
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.trumpRng = new Random(rng.nextLong());
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections
                .unmodifiableMap(new EnumMap<>(playerNames));
        cardsOfPlayers = new EnumMap<>(PlayerId.class);
        setPlayers();
        gameOver = false;

        shuffleAndDistributeCards();
        currentFirstPlayer = findFirstPlayer();
        newTurn(Score.INITIAL);
    }

    /**
     * Returns true iff the game is over.
     * 
     * @return true iff the game is over
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Makes the game progress to the end of the next trick, and nothing if the
     * game has ended.
     */
    public void advanceToEndOfNextTrick() {
        // If the game is over, we end the function
        if (isGameOver()) {
            updateScore();
            return;
        }

        // We collect the trick if it is full, and check if a team has won
        if (turnState.trick().isFull()) {
            turnState = turnState.withTrickCollected();
        }

        // If we have reached the end of a turn, we select the next player to
        // play, we redistribute the cards, and we start a new turn
        if (turnState.isTerminal()) {
            currentFirstPlayer = PlayerId.ALL
                    .get((currentFirstPlayer.ordinal() + 1) % PlayerId.COUNT);
            shuffleAndDistributeCards();
            newTurn(turnState.score().nextTurn());
        }

        // We aware the players of the current scores and the current empty
        // trick
        updateScore();
        updateTrick();

        existsWinningTeam();

        // We call the method play() until the trick is full
        while (!turnState.trick().isFull()) {
            play();
        }
    }

    private void existsWinningTeam() {
        for (TeamId teamId : TeamId.ALL) {
            if (PackedScore.totalPoints(turnState.packedScore(),
                    teamId) >= Jass.WINNING_POINTS) {
                for (Player player : players.values()) {
                    player.setWinningTeam(teamId);
                }
                gameOver = true;
            }
        }

    }

    // Creates a List containing all the cards of a Jass game.
    private List<Card> generateCards() {
        List<Card> deck = new LinkedList<>();
        for (int i = 0; i < CardSet.ALL_CARDS.size(); i++) {
            deck.add(CardSet.ALL_CARDS.get(i));
        }
        return deck;
    }

    // Tells each players who he is, and who are the others.
    private void setPlayers() {
        for (PlayerId playerId : players.keySet()) {
            players.get(playerId).setPlayers(playerId, playerNames);
        }
    }

    // Shuffles the deck of Cards and distributes cards to all the player.
    private void shuffleAndDistributeCards() {
        List<Card> allCards = new LinkedList<>(ALL_CARDS);
        Collections.shuffle(allCards, shuffleRng);
        for (PlayerId playerId : PlayerId.ALL) {
            CardSet playerHand = CardSet
                    .of(allCards.subList(playerId.ordinal() * Jass.HAND_SIZE,
                            (playerId.ordinal() + 1) * Jass.HAND_SIZE));
            cardsOfPlayers.put(playerId, playerHand);
            players.get(playerId).updateHand(playerHand);
        }
    }

    // Returns the first player of an overall Jass game : the one who has the
    // seven of diamond.
    private PlayerId findFirstPlayer() {
        for (PlayerId playerId : players.keySet()) {
            if (cardsOfPlayers.get(playerId)
                    .contains(Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN))) {
                return playerId;
            }
        }
        return null;
    }

    // Sets the turnState for a new turn, and calls updateTrump().
    private void newTurn(Score score) {
        Card.Color newTrump = players.get(currentFirstPlayer).selectTrump(trumpRng);
        turnState = TurnState.initial(newTrump, score, currentFirstPlayer);
        updateTrump(newTrump);
    }

    // Tells each player the current trump suit.
    private void updateTrump(Card.Color trump) {
        for (Player player : players.values()) {
            player.setTrump(turnState.trick().trump());
        }
    }

    // Tells each player what the current trick is.
    private void updateTrick() {
        for (PlayerId playerId : players.keySet()) {
            players.get(playerId).updateTrick(turnState.trick());
        }
    }

    // Tells each player the current scores.
    private void updateScore() {
        for (PlayerId playerId : players.keySet()) {
            players.get(playerId).updateScore(turnState.score());
        }
    }

    /*
     * Sets the turnState to a new state where a new card has been played, and
     * does the appropriate actions (removing card from the player's hand...).
     * It also calls updateTrick(), to inform each player that the trick has
     * changed.
     */
    private void play() {
        PlayerId currentPlayerId = turnState.nextPlayer();
        Player currentPlayer = players.get(currentPlayerId);
        CardSet currentCards = cardsOfPlayers.get(currentPlayerId);

        Card cardPlayed = currentPlayer.cardToPlay(turnState, currentCards);

        turnState = turnState.withNewCardPlayed(cardPlayed);
        cardsOfPlayers.put(currentPlayerId, currentCards.remove(cardPlayed));
        currentPlayer.updateHand(cardsOfPlayers.get(currentPlayerId));
        updateTrick();
    }

}
