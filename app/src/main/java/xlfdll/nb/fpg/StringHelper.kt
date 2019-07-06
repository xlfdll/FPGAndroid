package xlfdll.nb.fpg

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by Xlfdll on 2017/08/29.
 */

internal object StringHelper {
    fun getBytesString(bytes: ByteArray): String {
        val sb = StringBuilder()

        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }

        return sb.toString()
    }

    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun getHashString(hashAlgorithmName: String, text: String, encoding: String): String {
        val messageDigest = MessageDigest.getInstance(hashAlgorithmName)

        messageDigest.update(text.toByteArray(charset(encoding)))

        return StringHelper.getBytesString(messageDigest.digest())
    }
}