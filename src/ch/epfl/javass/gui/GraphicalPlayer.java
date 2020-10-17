package ch.epfl.javass.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Represents the graphical interface of a human player
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class GraphicalPlayer {
    private final PlayerId ownId;
    private final Map<PlayerId, String> playerNames;
    private final Scene scene;
    private BooleanProperty selectTrump = new SimpleBooleanProperty();
    private final int CARD_TRICK_WIDTH = 120;
    private final int CARD_TRICK_HEIGHT = 180;
    private final int CARD_HAND_WIDTH = 80;
    private final int CARD_HAND_HEIGHT = 120;
    private final double OPACITY_PLAYABLE = 1;
    private final double OPACITY_NOT_PLAYABLE = 0.2;
    private final int GAUSSIAN_BLUR_EFFECT = 4;
    private final int TRUMP_SIDE_SIZE = 101;
    private BooleanProperty invisibleHand = new SimpleBooleanProperty();

    private final static ObservableMap<Card, Image> cardAndImage160 = initializeCards(
            160);
    private final static ObservableMap<Card, Image> cardAndImage240 = initializeCards(
            240);
    private final static ObservableMap<Color, Image> trumpAndImage = initializeTrump();

    // Creates an observable map of Cards associated with Images of the given
    // size.
    private static ObservableMap<Card, Image> initializeCards(int size) {
        ObservableMap<Card, Image> cardAndImage = FXCollections
                .observableHashMap();
        for (Color color : Color.ALL) {
            for (Rank rank : Rank.ALL) {
                String imagePath = "/card_" + Integer.toString(color.ordinal())
                        + "_" + Integer.toString(rank.ordinal()) + "_"
                        + Integer.toString(size) + ".png";
                cardAndImage.put(Card.of(color, rank), new Image(imagePath));
            }
        }
        return cardAndImage;
    }

    // Creates an observable map of Colors associated with Images, for the trump
    // images.
    private static ObservableMap<Color, Image> initializeTrump() {
        ObservableMap<Color, Image> trumpAndImage = FXCollections
                .observableHashMap();
        for (Color color : Color.ALL) {
            String imagePath = "/trump_" + Integer.toString(color.ordinal())
                    + ".png";
            trumpAndImage.put(color, new Image(imagePath));
        }
        return trumpAndImage;
    }

    /**
     * GraphicalPlayer's constructor, creates the Scene and organizes the
     * components in it.
     * 
     * @param ownId
     *            : the underlying player id
     * @param playerNames
     *            : the name of all the players
     * @param scoreBean
     *            : the score bean
     * @param trickBean
     *            : the trick bean
     * @param handBean
     *            the hand bean
     * @param communicationQueueCard
     *            : the way used to communicate which card is played, one at a
     *            time
     */
    public GraphicalPlayer(PlayerId ownId, Map<PlayerId, String> playerNames,
            ScoreBean scoreBean, TrickBean trickBean, HandBean handBean,
            IntegerProperty timeProperty, BooleanProperty isPlayingProperty,
            ArrayBlockingQueue<Card> communicationQueueCard,
            ArrayBlockingQueue<Card.Color> communicationQueueColor) {
        this.ownId = ownId;
        this.playerNames = playerNames;
        selectTrump.set(false);
        invisibleHand.set(false);

        VBox centerPane = new VBox();

        BorderPane principalPane = new BorderPane();
        principalPane.setTop(createScorePane(scoreBean));
        principalPane.setCenter(createTrickPane(trickBean));
        principalPane
                .setBottom(createHandPane(handBean, communicationQueueCard));

        centerPane.getChildren().addAll(
                createTimerPane(timeProperty, isPlayingProperty),
                principalPane);

        Pane selectionPane = createSelectionPane(communicationQueueColor);

        StackPane fullPane = new StackPane();
        fullPane.getChildren().add(centerPane);
        for (TeamId team : TeamId.ALL) {
            fullPane.getChildren().add(createVictoryPane(scoreBean, team));
        }
        fullPane.getChildren().add(selectionPane);

        scene = new Scene(fullPane);
    }

    /**
     * Creates the Stage and adds title and scene informations.
     * 
     * @return the Stage created
     */
    public Stage createStage() {
        Stage stage = new Stage();
        stage.setTitle("Javass - " + playerNames.get(ownId));
        stage.setScene(scene);
        return stage;
    }

    /**
     * Sets selectTrump to true if the player need to select the trump.
     * 
     * @param state
     */
    public void selectTrump(boolean state) {
        selectTrump.set(state);
    }

    /**
     * Sets invisibleHand to true if the player take too much time to play.
     *
     * @param state
     */
    public void setViewHand(boolean state) {
        invisibleHand.set(state);
    }

    // Creates a Pane representing the Score.
    private Pane createScorePane(ScoreBean scoreBean) {
        GridPane scorePane = new GridPane();

        for (TeamId team : TeamId.ALL) {
            addScoreRow(scoreBean, team, scorePane);
        }

        scorePane.setStyle(
                "-fx-font: 16 Optima;" + "-fx-background-color: lightgray;"
                        + "-fx-padding: 5px;" + "-fx-alignment: center;");

        return scorePane;
    }

    // Creates a row of the Score representation, for a given team.
    private void addScoreRow(ScoreBean scoreBean, TeamId team,
            GridPane scorePane) {
        Text namesTeam = createNameTeam(team);

        Text turnPointsTeam = createTurnPointsTeam(scoreBean, team);

        Text pointsLastTrickTeam = createPointsLastTrick(scoreBean, team);

        Text total = new Text(" / Total : ");

        Text gamePointsTeam = createGamePointsTeam(scoreBean, team);

        scorePane.addRow(team.ordinal(), namesTeam, turnPointsTeam,
                pointsLastTrickTeam, total, gamePointsTeam);

        GridPane.setHalignment(namesTeam, HPos.RIGHT);
        GridPane.setHalignment(turnPointsTeam, HPos.RIGHT);
        GridPane.setHalignment(pointsLastTrickTeam, HPos.LEFT);
        GridPane.setHalignment(total, HPos.LEFT);
        GridPane.setHalignment(gamePointsTeam, HPos.RIGHT);
    }

    // Creates the names representation for a team.
    private Text createNameTeam(TeamId team) {
        StringJoiner sj = new StringJoiner(" et ");
        for (int i = 0; i < PlayerId.COUNT; i++) {
            PlayerId player = PlayerId.ALL
                    .get((ownId.ordinal() + i) % PlayerId.COUNT);
            if (player.team().equals(team)) {
                sj.add(playerNames.get(player));
            }
        }
        return new Text(sj.toString() + " : ");
    }

    // Creates the turn points representation for a team.
    private Text createTurnPointsTeam(ScoreBean scoreBean, TeamId team) {
        Text turnPointsTeam = new Text();
        turnPointsTeam.textProperty()
                .bind(Bindings.convert(scoreBean.turnPointsProperty(team)));
        return turnPointsTeam;
    }

    // Creates the last trick won points representation for a team.
    private Text createPointsLastTrick(ScoreBean scoreBean, TeamId team) {
        Text pointsLastTrickTeam = new Text();
        scoreBean.turnPointsProperty(team)
                .addListener((o, oV, nV) -> pointsLastTrickTeam.setText(
                        " (+" + Integer.toString(nV.intValue() - oV.intValue())
                                + ")"));
        scoreBean.gamePointsProperty(team)
                .addListener((o, oV, nV) -> pointsLastTrickTeam.setText(null));
        return pointsLastTrickTeam;
    }

    // Creates the game points representation for a team.
    private Text createGamePointsTeam(ScoreBean scoreBean, TeamId team) {
        Text gamePointsTeam = new Text();
        gamePointsTeam.textProperty()
                .bind(Bindings.convert(scoreBean.gamePointsProperty(team)));
        return gamePointsTeam;
    }

    // Creates a Pane representing the Trick.
    private Pane createTrickPane(TrickBean trickBean) {
        ImageView trump = new ImageView();
        trump.imageProperty().bind(
                Bindings.valueAt(trumpAndImage, trickBean.trumpProperty()));
        trump.setFitHeight(TRUMP_SIDE_SIZE);
        trump.setFitWidth(TRUMP_SIDE_SIZE);

        Map<PlayerId, ImageView> cardsPlayedByEachPlayer = new HashMap<>();

        for (PlayerId playerId : PlayerId.ALL) {
            ImageView card = new ImageView();
            card.imageProperty().bind(Bindings.valueAt(cardAndImage160,
                    Bindings.valueAt(trickBean.trickProperty(), playerId)));
            card.setFitWidth(CARD_TRICK_WIDTH);
            card.setFitHeight(CARD_TRICK_HEIGHT);
            cardsPlayedByEachPlayer.put(playerId, card);
        }

        PlayerId bottomPlayer = ownId;
        PlayerId rightPlayer = PlayerId.ALL
                .get((ownId.ordinal() + 1) % PlayerId.COUNT);
        PlayerId topPlayer = PlayerId.ALL
                .get((ownId.ordinal() + 2) % PlayerId.COUNT);
        PlayerId leftPlayer = PlayerId.ALL
                .get((ownId.ordinal() + 3) % PlayerId.COUNT);

        Pane bottomPlayerPane = createPlayerPane(trickBean, bottomPlayer,
                cardsPlayedByEachPlayer.get(bottomPlayer));
        Pane rightPlayerPane = createPlayerPane(trickBean, rightPlayer,
                cardsPlayedByEachPlayer.get(rightPlayer));
        Pane topPlayerPane = createPlayerPane(trickBean, topPlayer,
                cardsPlayedByEachPlayer.get(topPlayer));
        Pane leftPlayerPane = createPlayerPane(trickBean, leftPlayer,
                cardsPlayedByEachPlayer.get(leftPlayer));

        GridPane trickPane = new GridPane();

        trickPane.add(leftPlayerPane, 0, 0, 1, 3);
        trickPane.addColumn(1, topPlayerPane, trump, bottomPlayerPane);
        trickPane.add(rightPlayerPane, 2, 0, 1, 3);

        GridPane.setHalignment(trump, HPos.CENTER);

        trickPane.setStyle("-fx-background-color: whitesmoke;"
                + "-fx-padding: 5px;" + "-fx-border-width: 3px 0px;"
                + "-fx-border-style: solid;" + "-fx-border-color: gray;"
                + "-fx-alignment: center;");

        return trickPane;
    }

    // Creates a Pane containing the the player's name and the card played in
    // the current trick, or nothing if he did not played yet.
    private Pane createPlayerPane(TrickBean trickBean, PlayerId playerId,
            ImageView cardImageView) {
        Rectangle cardRectangle = new Rectangle(CARD_TRICK_WIDTH,
                CARD_TRICK_HEIGHT);
        cardRectangle.setEffect(new GaussianBlur(GAUSSIAN_BLUR_EFFECT));
        cardRectangle.setStyle("-fx-arc-width: 20;" + "-fx-arc-height: 20;"
                + "-fx-fill: transparent;" + "-fx-stroke: lightpink;"
                + "-fx-stroke-width: 5;" + "-fx-opacity: 0.5;");
        cardRectangle.visibleProperty()
                .bind(trickBean.winningPlayerProperty().isEqualTo(playerId));

        StackPane card = new StackPane();
        card.getChildren().addAll(cardImageView, cardRectangle);

        Text playerName = new Text(playerNames.get(playerId));
        playerName.setStyle("-fx-font: 14 Optima;");

        VBox player = new VBox();
        if (ownId.equals(playerId)) {
            player.getChildren().addAll(card, playerName);
        } else {
            player.getChildren().addAll(playerName, card);
        }
        player.setStyle("-fx-padding: 5px;" + "-fx-alignment: center;");

        return player;
    }

    // Creates the victory Pane for a Given team.
    private Pane createVictoryPane(ScoreBean scoreBean, TeamId team) {
        Text message = new Text();
        message.textProperty().bind(Bindings.format(
                "%s et %s ont gagné avec %d points contre %d.",
                playerNames.get(PlayerId.ALL.get(team.ordinal())),
                playerNames
                        .get(PlayerId.ALL.get(TeamId.COUNT + team.ordinal())),
                scoreBean.totalPointsProperty(team),
                scoreBean.totalPointsProperty(team.other())));

        BorderPane victoryPane = new BorderPane(message);
        victoryPane.setStyle(
                "-fx-font: 16 Optima;" + "-fx-background-color: white;");
        victoryPane.visibleProperty()
                .bind(scoreBean.winningTeamProperty().isEqualTo(team));

        return victoryPane;
    }

    // Creates a Pane representing the current Hand of the player.
    private Pane createHandPane(HandBean handBean,
            ArrayBlockingQueue<Card> communicationQueueCard) {
        StackPane handPane = new StackPane();

        Text message = new Text("Il fallait jouer plus vite...");
        BorderPane handPaneInvisible = new BorderPane(message);
        handPaneInvisible.setStyle(
                "-fx-font: 16 Optima;" + "-fx-background-color: white;");

        HBox handPaneVisible = new HBox();

        for (int i = 0; i < Jass.HAND_SIZE; i++) {
            ImageView cardView = new ImageView();
            ObjectBinding<Card> card = Bindings.valueAt(handBean.handProperty(),
                    i);
            cardView.imageProperty()
                    .bind(Bindings.valueAt(cardAndImage240, card));
            cardView.setFitHeight(CARD_HAND_HEIGHT);
            cardView.setFitWidth(CARD_HAND_WIDTH);
            BooleanBinding isPlayable = Bindings.createBooleanBinding(
                    () -> handBean.playableCards().contains(card.get()),
                    handBean.handProperty(), handBean.playableCards());
            cardView.opacityProperty().bind(Bindings.when(isPlayable)
                    .then(OPACITY_PLAYABLE).otherwise(OPACITY_NOT_PLAYABLE));

            cardView.setOnMouseClicked((e) -> {
                communicationQueueCard.offer(card.get());
            });
            handPaneInvisible.visibleProperty().bind(invisibleHand);
            handPaneVisible.getChildren().add(cardView);
        }

        handPaneVisible.setStyle("-fx-background-color: lightgray;"
                + "-fx-spacing: 5px;" + "-fx-padding: 5px;");

        handPane.getChildren().addAll(handPaneVisible, handPaneInvisible);

        return handPane;
    }

    private Pane createSelectionPane(
            ArrayBlockingQueue<Card.Color> communicationQueueColor) {
        HBox colors = new HBox();
        for (Card.Color color : Card.Color.ALL) {
            ImageView colorView = new ImageView();
            colorView.imageProperty().set(trumpAndImage.get(color));
            colorView.setOnMouseClicked((e) -> {
                communicationQueueColor.offer(color);
            });
            colorView.setFitHeight(TRUMP_SIDE_SIZE);
            colorView.setFitWidth(TRUMP_SIDE_SIZE);
            colors.getChildren().add(colorView);
        }
        colors.visibleProperty().bind(selectTrump);
        colors.setStyle("-fx-font: 16 Optima;" + "-fx-background-color: white;"
                + "-fx-spacing: 50px;" + "-fx-padding: 50px;");

        colors.setAlignment(Pos.CENTER);

        return colors;
    }

    private Pane createTimerPane(IntegerProperty timeProperty,
            BooleanProperty isPlayingProperty) {
        Text textTimer = new Text();
        textTimer.setStyle("-fx-font: 14 Optima;");

        timeProperty.addListener((o, oV, nV) -> textTimer.textProperty()
                .set("Timer: " + nV.toString()));

        HBox timer = new HBox(textTimer);
        timer.setAlignment(Pos.CENTER);
        timer.setStyle("-fx-font: 16 Optima;" + "-fx-background-color: tomato;"
                + "-fx-padding: 5px;" + "-fx-alignment: center;");
        timer.visibleProperty().bind(isPlayingProperty);

        return timer;
    }
}