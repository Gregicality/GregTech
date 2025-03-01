package gregtech.api.util;

import java.math.BigInteger;
import java.text.NumberFormat;

public class TextFormattingUtil {

    private static final long[] metricSuffixValues = {
            1_000L,
            1_000_000L,
            1_000_000_000L,
            1_000_000_000_000L,
            1_000_000_000_000_000L,
            1_000_000_000_000_000_000L
    };

    private static final BigInteger[] metricBigSuffixValues = {
            BigInteger.TEN.pow(3),
            BigInteger.TEN.pow(6),
            BigInteger.TEN.pow(9),
            BigInteger.TEN.pow(12),
            BigInteger.TEN.pow(15),
            BigInteger.TEN.pow(18),
            BigInteger.TEN.pow(21),
            BigInteger.TEN.pow(24),
            BigInteger.TEN.pow(27),
            BigInteger.TEN.pow(30)
    };

    private static final char[] metricSuffixChars = {
            'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'R', 'Q'
    };

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public static String formatLongToCompactString(long value, int precision) {
        if (value == 0 || Math.abs(value) < Math.pow(10, precision)) {
            return formatNumbers(value); // deal with easy case
        }

        StringBuilder stb = new StringBuilder();
        if (value < 0) {
            stb.append('-');
            // Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
            value = value == Long.MIN_VALUE ? Long.MAX_VALUE : -value;
        }

        int i = GTUtility.nearestLesserOrEqual(metricSuffixValues, value);
        if (i == -1) return stb.append(value).toString();

        long suffixValue = metricSuffixValues[i];
        stb.append(value / suffixValue);

        long truncatedDigit = value % suffixValue / (suffixValue / (long) Math.pow(10, precision - 3));
        if (truncatedDigit > 0) {
            stb.append('.').append(truncatedDigit);
        }
        return stb.append(metricSuffixChars[i]).toString();
    }

    public static String formatBigIntToCompactString(BigInteger value, int precision) {
        if (BigInteger.ZERO.equals(value) || value.abs().compareTo(BigInteger.TEN.pow(precision)) < 0) {
            return value.toString(); // deal with easy case
        }

        StringBuilder stb = new StringBuilder();
        if (value.signum() == -1) {
            stb.append('-');
            value = value.abs();
        }

        int c = 0;
        while (value.compareTo(metricBigSuffixValues[c]) >= 0) {
            c++;
        }

        return stb.append(value.divide(metricBigSuffixValues[c - 1]))
                .append('.')
                .append(value.toString(), 4, precision + 1)
                .append(metricSuffixChars[c - 1])
                .toString();
    }

    public static String formatLongToCompactString(long value) {
        return formatLongToCompactString(value, 3);
    }

    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    /** Allows for formatting Long, Integer, Short, Byte, Number, AtomicInteger, AtomicLong, and BigInteger. */
    public static String formatNumbers(Object number) {
        return NUMBER_FORMAT.format(number);
    }

    /**
     * Formats a string to multiple lines, attempting to place a new line at the closest space from "maxLength"
     * characters away.
     * 
     * @param toFormat  the string to be formatted to multiple lines.
     * @param maxLength the length where a newline should be placed in the nearest space.
     * @return a string formatted with newlines.
     */
    public static String formatStringWithNewlines(String toFormat, int maxLength) {
        String[] name = toFormat.split(" ");
        StringBuilder builder = new StringBuilder();
        int length = 0;
        for (String s : name) {
            length += s.length();

            if (length > maxLength) {
                builder.append("\n");
                builder.append(s);
                length = 0;
                continue;
            }

            if (builder.length() != 0)
                builder.append(" ");

            builder.append(s);
        }
        return builder.toString();
    }
}
