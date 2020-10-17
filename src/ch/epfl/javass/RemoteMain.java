package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Allows to play a distant game.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class RemoteMain extends Application {

    /**
     * Launches the game.
     * 
     * @param args
     *            : the arguments given to initialize the program (we will not
     *            use it here).
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("La partie commencera à la connexion du client...");
        Thread thread = new Thread(() -> {
            new RemotePlayerServer(new GraphicalPlayerAdapter()).run();
        });
        thread.setDaemon(true);
        thread.start();
    }

}
