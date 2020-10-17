package ch.epfl.javass;

/**
 * Provides methods to validate arguments.
 * 
 * @author Elior Papiernik (299399)
 */
public final class Preconditions {
    private Preconditions() {
    }

    /**
     * Checks if an argument is true and raises an exception if not.
     * 
     * @param b
     *            the argument to be checked
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException("The argument is false !");
        }
    }

    /**
     * Checks if an index is in the correct range for a given size and raises an
     * exception if not.
     * 
     * @param index
     *            the index to be tested
     * @param size
     *            the size of the list we want to access
     * @return the index, which is correct
     */
    public static int checkIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("The index is out of bounds");
        }
        return index;
    }
}