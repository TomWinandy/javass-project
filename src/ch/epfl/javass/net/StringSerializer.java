package ch.epfl.javass.net;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Provides static methods allowing to serialize and deserialize as Strings
 * values of type String, integer and long.
 * 
 * @author Elior Papiernik (299399)
 * @author Tom Winandy (302199)
 */
public final class StringSerializer {
    private final static int HEX_NUMBER = 0x10;
    private final static Charset CHARSET = StandardCharsets.UTF_8;

    private StringSerializer() {
    }

    /**
     * Serializes as a String an integer value.
     * 
     * @param toSerialize
     *            : the integer to serialize
     * @return the serialized form of the integer value
     */
    public static String serializeInt(int toSerialize) {
        return Integer.toUnsignedString(toSerialize, HEX_NUMBER);
    }

    /**
     * Deserializes an integer value serialized as a String.
     * 
     * @param serialized
     *            : the String to deserialize
     * @return the integer value of the serialized String
     */
    public static int deserializeInt(String serialized) {
        return Integer.parseUnsignedInt(serialized, HEX_NUMBER);
    }

    /**
     * Serializes as a String a long value.
     * 
     * @param toSerialize
     *            : the long to serialize
     * @return the serialized form of the long value
     */
    public static String serializeLong(long toSerialize) {
        return Long.toUnsignedString(toSerialize, HEX_NUMBER);
    }

    /**
     * Deserializes a long value serialized as a String.
     * 
     * @param serialized
     *            : the String to deserialize
     * @return the long value of the serialized String
     */
    public static long deserializeLong(String serialized) {
        return Long.parseUnsignedLong(serialized, HEX_NUMBER);
    }

    /**
     * Serializes as a String a String value.
     * 
     * @param toSerialize
     *            : the String to serialize
     * @return the serialized form of the String value
     */
    public static String serializeString(String toSerialize) {
        return Base64.getEncoder()
                .encodeToString(toSerialize.getBytes(CHARSET));
    }

    /**
     * Deserializes a String value serialized as a String.
     * 
     * @param serialized
     *            : the String to deserialize
     * @return the String value of the serialized String
     */
    public static String deserializeString(String serialized) {
        return new String(Base64.getDecoder().decode(serialized));
    }

    /**
     * Returns the String obtained by the concatenation of all the given
     * Strings, separated by the given character.
     * 
     * @param sepChar
     *            : the separation character
     * @param toCombine
     *            : all the Strings to concatenate
     * @return the combined form of the arguments
     */
    public static String combine(char sepChar, String... toCombine) {
        return String.join(Character.toString(sepChar), toCombine);
    }

    /**
     * Returns an array of Strings obtained by the separation of the given
     * String according to every case of the given character.
     * 
     * @param sepChar
     *            : the separation character
     * @param toSplit
     *            : the String to split
     * @return the String array obtained as described
     */
    public static String[] split(char sepChar, String toSplit) {
        return toSplit.split(Character.toString(sepChar));
    }
}