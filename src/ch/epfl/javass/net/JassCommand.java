package ch.epfl.javass.net;

/**
 * Lists the type of messages exchanged between the client and the server.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public enum JassCommand {
    PLRS,
    TRMP,
    HAND,
    TRCK,
    CARD,
    SCOR,
    WINR,
    SLCT;

    public static final int COMMAND_SIZE = 4;
}