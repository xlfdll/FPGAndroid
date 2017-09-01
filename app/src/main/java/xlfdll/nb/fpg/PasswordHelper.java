package xlfdll.nb.fpg;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by Xlfdll on 2017/08/29.
 */

class PasswordHelper {
    public static final int RandomSaltLength = 64;
    public static final String RandomSaltBackupDataFileName = "FPG_Salt.dat";
    public static final String RandomSaltBackupDataMIMEType = "text/plain";

    public static String generatePassword(Context context, String keyword, String salt, int length)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();

        sb.append(keyword);
        sb.append(AppHelper.Settings.getString(context.getString(R.string.pref_key_randomsalt),
                PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength)));
        sb.append(salt);

        String result = StringHelper.getHashString("SHA-512", sb.toString(), "UTF-16BE");

        sb = new StringBuilder();

        for (int i = 0, j = length; i < length; i++, j++) {
            if ((j + i) == result.length()) {
                j = 0;
            }

            sb.append(i % 2 == 0 ? Character.toUpperCase(result.charAt(j + i)) : result.charAt(j + i));
        }

        return sb.toString();
    }

    public static String generateSalt(int length) {
        String basicCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_ []{}<>~`+=,.;:/?|";

        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(basicCharacters.length() - 1);
            sb.append(basicCharacters.substring(index, index + 1));
        }

        return sb.toString();
    }

    public static File getRandomSaltFile(Context context) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    PasswordHelper.RandomSaltBackupDataFileName);
        } else {
            return new File(context.getFilesDir(), PasswordHelper.RandomSaltBackupDataFileName);
        }
    }

    public static String loadRandomSalt(File file)
            throws IOException {
        if (file != null) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            String line = reader.readLine();

            reader.close();

            return line;
        }

        // File not found
        return null;
    }

    public static void saveRandomSalt(File file, String randomSalt)
            throws IOException {
        if (file != null) {
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            writer.write(randomSalt);
            writer.close();
        }
    }
}