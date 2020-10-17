package ch.epfl.javass.bits;


import ch.epfl.javass.Preconditions;

/**
 * Provides static methods that allows working on 32 bits vectors stored in
 * integer values.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class Bits32 {
    private Bits32() {
    }

    /**
     * Returns a 32 bits mask composed of zeros and a sequence of ones from
     * index start (included) to index start + size (excluded).
     * 
     * @param start
     *            the index where the sequence of 1 starts
     * @param size
     *            the size of the sequence of 1
     * @return the mask created
     */
    public static int mask(int start, int size) {
        Preconditions.checkArgument(isRangeValid(start, size));
        if (isOverflowCase(start, size)) {
            return -1;
        }
        return ((1 << size) - 1) << start;
    }

    /**
     * Returns an integer having the value of the bits sequence from index start
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
    public static int extract(int bits, int start, int size) {
        return (bits & mask(start, size)) >>> start;
    }

    /**
     * Returns an integer containing v1 and v2, respectively encoded in the s1
     * least significant bits and in the s2 following bits. All the unused bits
     * are set to 0.
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
    public static int pack(int v1, int s1, int v2, int s2) {
        Preconditions.checkArgument(isPairValid(v1, s1));
        Preconditions.checkArgument(isPairValid(v2, s2));
        Preconditions.checkArgument(s1 + s2 <= Integer.SIZE);
        return v2 << s1 | v1;
    }

    /**
     * Returns an integer containing v1, v2 and v3, respectively encoded in the
     * s1 least significant bits, in the s2 and s3 following bits. All the
     * unused bits are set to 0.
     * 
     * @param v1
     *            the first value to encode
     * @param s1
     *            the number of bits to encode it in
     * @param v2
     *            the second value to encode
     * @param s2
     *            the number of bits to encode it in
     * @param v3
     *            the third value to encode
     * @param s3
     *            the number of bits to encode it in
     * @return the integer formed
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        return pack(pack(v1, s1, v2, s2), s1 + s2, v3, s3);
    }

    /**
     * Returns an integer containing v1, v2, v3, v4, v5, v6 and v7, respectively
     * encoded in the s1 least significant bits, in the s2, s3, s4, s5, s6 and
     * s7 in following bits. All the unused bits are set to 0.
     * 
     * @param v1
     *            the first value to encode
     * @param s1
     *            the number of bits to encode it in
     * @param v2
     *            the second value to encode
     * @param s2
     *            the number of bits to encode it in
     * @param v3
     *            the third value to encode
     * @param s3
     *            the number of bits to encode it in
     * @param v4
     *            the fourth value to encode
     * @param s4
     *            the number of bits to encode it in
     * @param v5
     *            the fifth value to encode
     * @param s5
     *            the number of bits to encode it in
     * @param v6
     *            the sixth value to encode
     * @param s6
     *            the number of bits to encode it in
     * @param v7
     *            the seventh value to encode
     * @param s7
     *            the number of bits to encode it in
     * @return the integer formed
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3,
            int v4, int s4, int v5, int s5, int v6, int s6, int v7, int s7) {
        return pack(pack(pack(v1, s1, v2, s2, v3, s3), s1 + s2 + s3, v4, s4, v5,
                s5), s1 + s2 + s3 + s4 + s5, v6, s6, v7, s7);
    }

    private static boolean isOverflowCase(int start, int size) {
        return start == 0 && size == Integer.SIZE;
    }

    private static boolean isRangeValid(int start, int size) {
        return (start >= 0 && size >= 0 && start + size <= Integer.SIZE);
    }

    private static boolean isPairValid(int v, int s) {
        return s >= 1 && s < Integer.SIZE && extract(v, 0, s) == v;
    }
}