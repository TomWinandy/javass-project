package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

/**
 * JavaFX bean containing the hand and the playable cards.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class HandBean {

    private final ObservableList<Card> hand = initHand();
    private final ObservableSet<Card> playableCards = FXCollections
            .observableSet();

    /**
     * Returns the hand property.
     * 
     * @return the hand property
     */
    public ObservableList<Card> handProperty() {
        return FXCollections.unmodifiableObservableList(hand);
    }

    /**
     * Enables to modify the value contained by the hand property.
     * 
     * @param newHand
     *            the new value of the property
     */
    public void setHand(CardSet newHand) {
        if (newHand.size() == Jass.HAND_SIZE) {
            for (int i = 0; i < newHand.size(); i++) {
                hand.set(i, newHand.get(i));
            }
        } else {
            for (Card card : hand) {
                if (card != null && !newHand.contains(card)) {
                    hand.set(hand.indexOf(card), null);
                }
            }
        }
    }

    /**
     * Returns the playableCards property.
     * 
     * @return the playableCards property
     */
    public ObservableSet<Card> playableCards() {
        return FXCollections.unmodifiableObservableSet(playableCards);
    }

    /**
     * Enables to modify the value contained by the playableCards property.
     * 
     * @param newPlayableCards
     *            the new value of the property
     */
    public void setPlayableCards(CardSet newPlayableCards) {
        playableCards.clear();
        for (int i = 0; i < newPlayableCards.size(); i++) {
            playableCards.add(newPlayableCards.get(i));
        }
    }

    private ObservableList<Card> initHand() {
        ObservableList<Card> firstHand = FXCollections.observableArrayList();
        for (int i = 0; i < Jass.HAND_SIZE; i++) {
            firstHand.add(null);
        }
        return firstHand;
    }

}