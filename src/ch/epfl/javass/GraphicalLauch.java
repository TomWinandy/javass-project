package ch.epfl.javass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.PlayerId;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Represents the initialization graphical interface.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class GraphicalLauch {
    private final ArrayBlockingQueue<List<String>> communicationQueue;
    private final Scene scene;
    private Stage stage;

    /**
     * GraphicalLauch's constructor.
     * 
     * @param communicationQueue
     */
    public GraphicalLauch(ArrayBlockingQueue<List<String>> communicationQueue) {
        this.communicationQueue = communicationQueue;

        GridPane globalPane = new GridPane();

        HBox param = new HBox();

        Map<PlayerId, String> playersType = new HashMap<>();

        Map<PlayerId, TextField> textFieldsNames = new HashMap<>();
        Map<PlayerId, TextField> textFieldsIterations = new HashMap<>();
        Map<PlayerId, TextField> textFieldsHost = new HashMap<>();

        for (PlayerId playerId : PlayerId.ALL) {
            VBox player = new VBox();

            Text playerIdText = new Text(playerId.name());

            HBox typeSelection = new HBox();
            List<ToggleButton> toggleButtons = new ArrayList<>();
            ToggleGroup typeGroup = new ToggleGroup();

            ToggleButton humanButton = new ToggleButton("Human");
            humanButton.setToggleGroup(typeGroup);
            humanButton.setOnAction((e) -> playersType.put(playerId, "human"));
            ToggleButton simulatedButton = new ToggleButton("Simulated");
            simulatedButton.setToggleGroup(typeGroup);
            simulatedButton
                    .setOnAction((e) -> playersType.put(playerId, "simulated"));
            ToggleButton remoteButton = new ToggleButton("Remote");
            remoteButton.setToggleGroup(typeGroup);
            remoteButton
                    .setOnAction((e) -> playersType.put(playerId, "remote"));

            toggleButtons.addAll(
                    Arrays.asList(humanButton, simulatedButton, remoteButton));
            typeSelection.getChildren().addAll(toggleButtons);

            TextField playerName = new TextField(
                    LocalMain.DEFAULT_NAMES.get(playerId));
            textFieldsNames.put(playerId, playerName);

            StackPane thirdParameter = new StackPane();

            TextField iterations = new TextField(
                    Integer.toString(LocalMain.DEFAULT_MCTS_ITERATIONS));
            iterations.visibleProperty()
                    .bind(Bindings.createBooleanBinding(
                            () -> simulatedButton.isSelected(),
                            typeGroup.selectedToggleProperty()));
            textFieldsIterations.put(playerId, iterations);

            TextField host = new TextField(LocalMain.DEFAULT_HOST_NAME);
            host.visibleProperty()
                    .bind(Bindings.createBooleanBinding(
                            () -> remoteButton.isSelected(),
                            typeGroup.selectedToggleProperty()));
            textFieldsHost.put(playerId, host);

            thirdParameter.getChildren().addAll(iterations, host);

            player.setAlignment(Pos.CENTER);
            player.getChildren().addAll(playerIdText, typeSelection, playerName,
                    thirdParameter);

            param.getChildren().add(player);
        }

        VBox rngSeedBox = new VBox();

        IntegerProperty rngSeed = new SimpleIntegerProperty();
        
        Text label = new Text("Random seed");
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        spinner.setValueFactory(factory);

        spinner.valueProperty().addListener((o) -> rngSeed.set(factory.getValue()));
        
        rngSeedBox.getChildren().addAll(label, spinner);

        param.getChildren().add(rngSeedBox);

        Button confirmation = new Button("Start the game !");
        confirmation.setOnAction((a) -> {
            List<String> parameters = new LinkedList<>();
            for (PlayerId playerId : PlayerId.ALL) {
                switch (playersType.get(playerId)) {
                case "human":
                    parameters.add(String.join(":", "h",
                            textFieldsNames.get(playerId).getText()));
                    break;
                case "simulated":
                    parameters.add(String.join(":", "s",
                            textFieldsNames.get(playerId).getText(),
                            textFieldsIterations.get(playerId).getText()));
                    break;
                case "remote":
                    parameters.add(String.join(":", "r",
                            textFieldsNames.get(playerId).getText(),
                            textFieldsHost.get(playerId).getText()));
                    break;
                }
            }
            parameters.add(Integer.toString(rngSeed.get()));
            communicationQueue.offer(parameters);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            stage.close();
        });

        globalPane.addRow(0, param);
        globalPane.addRow(1, confirmation);
        globalPane.setAlignment(Pos.CENTER);
        GridPane.setHalignment(confirmation, HPos.CENTER);

        scene = new Scene(globalPane);
    }

    public Stage createStage() {
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Players type selection");
        return stage;
    }
}
