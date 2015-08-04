package droidkit.crypto;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

/**
 * @author Daniel Serdyukov
 */
public final class Hex {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final char[] UPPER_CASE_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private Hex() {
    }

    @NonNull
    public static String toHexString(@NonNull String data) {
        return toHexString(data, false);
    }

    @NonNull
    public static String toHexString(@NonNull String data, boolean upperCase) {
        return toHexString(data.getBytes(UTF_8), upperCase);
    }

    @NonNull
    public static String toHexString(@NonNull byte[] data) {
        return toHexString(data, false);
    }

    @NonNull
    public static String toHexString(@NonNull byte[] data, boolean upperCase) {
        final StringBuilder sb = new StringBuilder(data.length * 2);
        for (final byte b : data) {
            sb.append(byteToHexString(b, upperCase));
        }
        return sb.toString().trim();
    }

    @NonNull
    public static byte[] fromHexString(@NonNull String hex) {
        final int length = hex.length();
        final byte[] bytes = new byte[length / 2];
        for (int i = 0, k = 0; i + 1 < length; i += 2, k++) {
            bytes[k] = (byte) (Character.digit(hex.charAt(i), 16) << 4);
            bytes[k] += (byte) (Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    @NonNull
    public static String byteToHexString(byte b, boolean upperCase) {
        final char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        final char[] buf = new char[]{digits[(b >> 4) & 0xf], digits[b & 0xf]};
        return new String(buf, 0, 2);
    }

}
