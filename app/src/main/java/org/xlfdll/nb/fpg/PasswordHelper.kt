package org.xlfdll.nb.fpg

import android.content.Context
import android.os.Environment

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.security.NoSuchAlgorithmException
import java.util.Random

/**
 * Created by Xlfdll on 2017/08/29.
 */

internal object PasswordHelper {
    internal const val RandomSaltLength = 64
    internal const val RandomSaltBackupDataFileName = "FPG_Salt.dat"
    internal const val RandomSaltBackupDataMIMEType = "text/plain"

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    internal fun generatePassword(context: Context, keyword: String, salt: String, length: Int): String {
        var sb = StringBuilder()

        sb.append(keyword)
        sb.append(AppHelper.Settings.getString(context.getString(R.string.pref_key_random_salt),
                PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength)))
        sb.append(salt)

        val result = StringHelper.getHashString("SHA-512", sb.toString(), "UTF-16BE")

        sb = StringBuilder()

        var i = 0
        var j = length
        while (i < length) {
            if (j + i == result.length) {
                j = 0
            }

            sb.append(if (i % 2 == 0) Character.toUpperCase(result[j + i]) else result[j + i])
            i++
            j++
        }

        return sb.toString()
    }

    internal fun generateSalt(length: Int): String {
        val basicCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_ []{}<>~`+=,.;:/?|"

        val sb = StringBuilder()
        val random = Random()

        for (i in 0 until length) {
            val index = random.nextInt(basicCharacters.length - 1)
            sb.append(basicCharacters.substring(index, index + 1))
        }

        return sb.toString()
    }

    internal fun getRandomSaltFile(context: Context): File {
        val state = Environment.getExternalStorageState()

        return if (Environment.MEDIA_MOUNTED == state) {
            File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    PasswordHelper.RandomSaltBackupDataFileName)
        } else {
            File(context.filesDir, PasswordHelper.RandomSaltBackupDataFileName)
        }
    }

    @Throws(IOException::class)
    internal fun loadRandomSalt(file: File?): String? {
        if (file != null) {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))

            val line = reader.readLine()

            reader.close()

            return line
        }

        // File not found
        return null
    }

    @Throws(IOException::class)
    internal fun saveRandomSalt(file: File?, randomSalt: String) {
        if (file != null) {
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8"))

            writer.write(randomSalt)
            writer.close()
        }
    }
}