package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * Provides static methods that allows working on 64 bits vectors stored in
 * integer values.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class Bits64 {
    private Bits64() {
    }

    /**
     * Returns a 64 bits mask composed of zeros and a sequence of ones from
     * index start (included) to index start + size (excluded).
     * 
     * @param start
     *            the index where the sequence of 1 starts
     * @param size
     *            the size of the sequence of 1
     * @return the mask created
     */
    public static long mask(int start, int size) {
        Preconditions.checkArgument(isRangeValid(start, size));
        if (isOverflowCase(start, size)) {
            return -1;
        }
        return ((1L << size) - 1) << start;
    }

    /**
     * Returns a long having the value of the bits sequence from index start
     * (included) to index start + size (excluded).
     * 
     * @param bits
     *            the value to extract the bits sequence from
     * @param start
     *            the index where to start the extraction
     * @param size
     *            the size of the bits sequence to extract
     * @return the extracted value
     */
    public static long extract(long bits, int start, int size) {
        Preconditions.checkArgument(isRangeValid(start, size));
        return (bits & mask(start, size)) >>> start;
    }

    /**
     * Returns a long containing v1 and v2, respectively encoded in the s1 least
     * significant bits and in the s2 following bits. All the unused bits are
     * set to 0.
     * 
     * @param v1
     *            the first value to encode
     * @param s1
     *            the number of bits to encode it in
     * @param v2
     *            the second value to encode
     * @param s2
     *            the number of bits to encode it in
     * @return the integer formed
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        Preconditions.checkArgument(isPairValid(v1, s1));
        Preconditions.checkArgument(isPairValid(v2, s2));
        Preconditions.checkArgument(s1 + s2 <= Long.SIZE);
        return v2 << s1 | v1;
    }

    private static boolean isOverflowCase(int start, int size) {
        return start == 0 && size == Long.SIZE;
    }

    private static boolean isRangeValid(int start, int size) {
        return start >= 0 && size >= 0 && start + size <= Long.SIZE;
    }

    private static boolean isPairValid(long v, int s) {
        return s >= 1 && s < Long.SIZE && extract(v, 0, s) == v;
    }
}