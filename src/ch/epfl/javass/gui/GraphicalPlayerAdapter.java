package ch.epfl.javass.gui;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Adapter for using a GraphicalPlayer as a Player.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class GraphicalPlayerAdapter implements Player {
    private final ScoreBean scoreBean;
    private final TrickBean trickBean;
    private final HandBean handBean;
    private final IntegerProperty timeProperty;
    private final BooleanProperty isPlayingProperty;
    private GraphicalPlayer graphicalPlayer;
    private final ArrayBlockingQueue<Card> communicationQueueCard;
    private final ArrayBlockingQueue<Card.Color> communicationQueueColor;

    private final int MILLIS_IN_SECOND = 1000;
    private final long AUTOPLAY_PAUSE_TIME = 2L;
    private final int MIN_TIME_TO_PLAY = 5;
    private final int QUEUE_SIZE = 1;

    /**
     * GraphicalPlayerAdapter constructor, initializes all instance variables
     * except the graphical interface.
     */
    public GraphicalPlayerAdapter() {
        scoreBean = new ScoreBean();
        trickBean = new TrickBean();
        handBean = new HandBean();
        timeProperty = new SimpleIntegerProperty();
        isPlayingProperty = new SimpleBooleanProperty(false);

        communicationQueueCard = new ArrayBlockingQueue<>(QUEUE_SIZE);
        communicationQueueColor = new ArrayBlockingQueue<>(QUEUE_SIZE);
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {

        isPlayingProperty.set(true);

        Platform.runLater(() -> handBean
                .setPlayableCards(state.trick().playableCards(hand)));

        try {
            long start = System.currentTimeMillis();

            do {
                if (communicationQueueCard.peek() != null) {
                    Card card = communicationQueueCard.take();
                    Platform.runLater(
                            () -> handBean.setPlayableCards(CardSet.EMPTY));
                    return card;
                }

                timeProperty.set((int) (MIN_TIME_TO_PLAY
                        + (start - System.currentTimeMillis())
                                / MILLIS_IN_SECOND));

            } while (timeProperty.get() > 0);

            graphicalPlayer.setViewHand(true);
            Thread.sleep(AUTOPLAY_PAUSE_TIME * MILLIS_IN_SECOND);
            Platform.runLater(() -> handBean.setPlayableCards(CardSet.EMPTY));
            
            return state.trick().playableCards(hand).get(0);

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            graphicalPlayer.setViewHand(false);
            isPlayingProperty.set(false);
        }

    }

    @Override
    public Color selectTrump(Random trumpRng) {
        graphicalPlayer.selectTrump(true);
        try {
            Card.Color newTrump = communicationQueueColor.take();
            graphicalPlayer.selectTrump(false);
            return newTrump;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        graphicalPlayer = new GraphicalPlayer(ownId, playerNames, scoreBean,
                trickBean, handBean, timeProperty, isPlayingProperty,
                communicationQueueCard, communicationQueueColor);
        Platform.runLater(() -> {
            graphicalPlayer.createStage().show();
        });
    }

    @Override
    public void updateHand(CardSet newHand) {
        Platform.runLater(() -> {
            handBean.setHand(newHand);
        });
    }

    @Override
    public void setTrump(Color trump) {
        Platform.runLater(() -> {
            trickBean.setTrump(trump);
        });
    }

    @Override
    public void updateTrick(Trick newTrick) {
        Platform.runLater(() -> {
            trickBean.setTrick(newTrick);
        });
    }

    @Override
    public void updateScore(Score score) {
        for (TeamId team : TeamId.ALL) {
            Platform.runLater(() -> {
                scoreBean.setTurnPoints(team, score.turnPoints(team));
                scoreBean.setTotalPoints(team, score.totalPoints(team));
                scoreBean.setGamePoints(team, score.gamePoints(team));
            });
        }
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        Platform.runLater(() -> {
            scoreBean.setWinningTeam(winningTeam);
        });
    }
}