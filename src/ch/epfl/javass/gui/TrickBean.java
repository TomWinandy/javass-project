package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * JavaFX bean containing the current trick.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class TrickBean {

    private final ObjectProperty<Color> trump = new SimpleObjectProperty<Color>();
    private final ObservableMap<PlayerId, Card> trick = FXCollections
            .observableHashMap();
    private final ObjectProperty<PlayerId> winningPlayer = new SimpleObjectProperty<PlayerId>();

    /**
     * Returns the property trump.
     * 
     * @return the property trump
     */
    public ReadOnlyObjectProperty<Color> trumpProperty() {
        return trump;
    }

    /**
     * Enable to modify the value contained by the property trump.
     * 
     * @param newTrump
     *            the new value of the property
     */
    public void setTrump(Color newTrump) {
        trump.setValue(newTrump);
    }

    /**
     * Returns the property trick.
     * 
     * @return the property trick
     */
    public ObservableMap<PlayerId, Card> trickProperty() {
        return FXCollections.unmodifiableObservableMap(trick);
    }

    /**
     * Enables to modify the value contained by the trick property. It also
     * updates the value contained in the winningPlayer property.
     * 
     * @param newTrick
     *            the new value of the property
     */
    public void setTrick(Trick newTrick) {
        for (int i = 0; i < PlayerId.COUNT; i++) {
            trick.put(newTrick.player(i),
                    i < newTrick.size() ? newTrick.card(i) : null);
        }
        winningPlayer
                .setValue(newTrick.isEmpty() ? null : newTrick.winningPlayer());
    }

    /**
     * Returns the property winningPlayer.
     * 
     * @return the property winningPlayer
     */
    public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
        return winningPlayer;
    }

}