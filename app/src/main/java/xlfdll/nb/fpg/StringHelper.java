package xlfdll.nb.fpg;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Xlfdll on 2017/08/29.
 */

class StringHelper {
    public static String getBytesString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static String getHashString(String hashAlgorithmName, String text, String encoding)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithmName);

        messageDigest.update(text.getBytes(encoding));

        return StringHelper.getBytesString(messageDigest.digest());
    }
}