package ch.epfl.javass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Contains the program allowing to play a game.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class LocalMain extends Application {
    
    private ArrayBlockingQueue<List<String>> communicationQueue;

    private final int NUMBER_ARGUMENTS_WITHOUT_SEED = 4;
    private final int NUMBER_ARGUMENTS_WITH_SEED = 5;
    private final int MIN_ITERATIONS_MCTS = 10;
    private final long MIN_TIME_PACED_PLAYER = 2;
    private final long MIN_TIME_END_TRICK = 1000;
    private final int EXIT_CODE = 1;
    public final static int DEFAULT_MCTS_ITERATIONS = 10000;
    public final static String DEFAULT_HOST_NAME = "localhost";
    public final static Map<PlayerId, String> DEFAULT_NAMES = initDefaultNames();
    
    private final int PLAYER_TYPE_INDEX = 0;
    private final int NAME_INDEX = 1;
    private final int R_IP_INDEX = 2;
    private final int S_NUMBER_ITERATIONS_INDEX = 2;
    private final int MAX_ARGUMENTS_H_PLAYER = 2;
    private final int MAX_ARGUMENTS_S_PLAYER = 3;
    private final int MAX_ARGUMENTS_R_PLAYER = 3;
    

    private static Map<PlayerId, String> initDefaultNames() {
        Map<PlayerId, String> names = new HashMap<>();
        names.put(PlayerId.PLAYER_1, "Aline");
        names.put(PlayerId.PLAYER_2, "Bastien");
        names.put(PlayerId.PLAYER_3, "Colette");
        names.put(PlayerId.PLAYER_4, "David");
        return names;
    }
    
    /**
     * Launches the game.
     * 
     * @param args
     *            : the arguments given to initialize the program (player's
     *            type, name, seed, iterations, IP address)
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    // This method analyzes the arguments and creates the 4 players, indicating
    // any errors encountered.
    // If the 4 players can be created without errors, a separate thread is
    // started, in which the game takes place.
    public void start(Stage primaryStage) throws Exception {
        communicationQueue = new ArrayBlockingQueue<>(1);
        startThread();
    }

    // Creates the JassGame and make the game happen !
    private void startThread() {
        Thread gameThread = new Thread(() -> {

            // ==============================================================================
            
            GraphicalLauch gl = new GraphicalLauch(communicationQueue);
            Platform.runLater(() -> {
                gl.createStage().show();
            });

            
            List<String> listParameters = null;
            try {
                listParameters = communicationQueue.take();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            

            int numberArguments = listParameters.size();
            if (numberArguments != NUMBER_ARGUMENTS_WITHOUT_SEED
                    && numberArguments != NUMBER_ARGUMENTS_WITH_SEED) {
                System.err.println(
                        "Utilization: java ch.epfl.javass.LocalMain :\n"
                + "<p1> <p2> <p3> <p4> [<seed>] where :\n"
                + "<jn> specifies the n-th player :\n"
                + "   h:<name> a human player named <name>\n"
                + "   s:<name>:<iterations> a simulated player named <name> with <iterations> iterations for the MctsPlayer\n"
                + "   r:<name>:<hostPort> a remote player named <name>, connected on port <hostPort>\n"
                + "   [<seed>] is the seed used to generate random numbers for JassGame and MctsPlayer\n\n"
                + "Many arguments are optionnal and have a default value :\n"
                + "   <name>       : Aline, Bastien, Colette, David\n"
                + "   <iterations> : 10_000\n"
                + "   <host>       : localhost\n"
                + "   [<seed>]     : no default value, the program creates a Random object with no seed");
                System.exit(EXIT_CODE);
            }

            // ==============================================================================

            List<List<String>> arguments = new ArrayList<List<String>>();
            listParameters
                    .forEach((o) -> arguments.add(Arrays.asList(o.split(":"))));

            // ==============================================================================

            Random rng;
            if (numberArguments == NUMBER_ARGUMENTS_WITH_SEED) {
                String seedString = listParameters.get(NUMBER_ARGUMENTS_WITH_SEED - 1);
                long seed = 0;
                try {
                    seed = Long.parseLong(seedString);
                } catch (NumberFormatException e) {
                    System.err.println(
                            "Erreur : La graine aléatoire n'est pas un entier long valid : "
                                    + seedString);
                    System.exit(EXIT_CODE);
                }
                rng = new Random(seed);
            } else {
                rng = new Random();
            }

            // ==============================================================================

            long rngJass = rng.nextLong();

            Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
            Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);

            for (PlayerId player : PlayerId.ALL) {
                playerNames.put(player, DEFAULT_NAMES.get(player));
            }

            Iterator<List<String>> argument = arguments.iterator();
            for (PlayerId player : PlayerId.ALL) {
                long rngSeed = rng.nextLong();
                List<String> playerInformations = argument.next();

                if (playerInformations.size() > NAME_INDEX) {
                    if (playerInformations.get(NAME_INDEX) != "") {
                        playerNames.put(player, playerInformations.get(NAME_INDEX));
                    }
                }

                String playerType = playerInformations.get(PLAYER_TYPE_INDEX);

                switch (playerType) {

                case "h":
                    if (playerInformations.size() > MAX_ARGUMENTS_H_PLAYER) {
                        System.err.println(
                                "Erreur : La spécification d'un joueur h comporte maximum " + MAX_ARGUMENTS_H_PLAYER + " composantes : "
                                        + playerInformations.toString());
                        System.exit(EXIT_CODE);
                    }
                    players.put(player, new GraphicalPlayerAdapter());
                    break;

                case "s":
                    int nbIterations = DEFAULT_MCTS_ITERATIONS;
                    if (playerInformations.size() > MAX_ARGUMENTS_S_PLAYER) {
                        System.err.println(
                                "Erreur : La spécification d'un joueur s comporte maximum " + MAX_ARGUMENTS_S_PLAYER + " composantes : "
                                        + playerInformations.toString());
                        System.exit(EXIT_CODE);
                    } else if (playerInformations.size() == MAX_ARGUMENTS_S_PLAYER) {
                        try {
                            nbIterations = Integer
                                    .parseInt(playerInformations.get(S_NUMBER_ITERATIONS_INDEX));
                        } catch (NumberFormatException e) {
                            System.err.println(
                                    "Erreur : Le nombre d'itérations n'est pas un entier int valid : "
                                            + playerInformations.get(S_NUMBER_ITERATIONS_INDEX));
                            System.exit(EXIT_CODE);
                        }

                        if (nbIterations < MIN_ITERATIONS_MCTS) {
                            System.err.println(
                                    "Erreur : Le nombre d'itérations du joueur simulé est inférieur à " + MIN_ITERATIONS_MCTS + " : "
                                            + nbIterations);
                            System.exit(EXIT_CODE);
                        }
                    }

                    players.put(player, new PacedPlayer(
                            new MctsPlayer(player, rngSeed, nbIterations),
                            MIN_TIME_PACED_PLAYER));
                    break;

                case "r":
                    String hostName = DEFAULT_HOST_NAME;
                    if (playerInformations.size() > MAX_ARGUMENTS_R_PLAYER) {
                        System.err.println(
                                "Erreur : La spécification d'un joueur r comporte maximum " + MAX_ARGUMENTS_R_PLAYER + " composantes : "
                                        + playerInformations.toString());
                        System.exit(EXIT_CODE);
                    } else if (playerInformations.size() == MAX_ARGUMENTS_R_PLAYER) {
                        hostName = playerInformations.get(R_IP_INDEX);
                    }
                    
                    try {
                        players.put(player, new RemotePlayerClient(hostName));
                    } catch (IOException e) {
                        System.err.println(
                                "Erreur : Une erreur s'est produite lors de la connexion au serveur d'un joueur distant : " + hostName);
                        System.exit(EXIT_CODE);
                    }
                    break;

                default:
                    System.err.println(
                            "Erreur : La première composante d'une spécification de joueur doit être h, s ou r : "
                                    + playerType);
                    System.exit(EXIT_CODE);
                    break;
                }
            }
            
            JassGame jassGame = new JassGame(rngJass, players, playerNames);
            while (!jassGame.isGameOver()) {
                jassGame.advanceToEndOfNextTrick();
                try {
                    Thread.sleep(MIN_TIME_END_TRICK);
                } catch (Exception e) {
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }
}
