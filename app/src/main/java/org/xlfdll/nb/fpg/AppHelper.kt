package org.xlfdll.nb.fpg

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog

/**
 * Created by Xlfdll on 2017/08/31.
 */

internal object AppHelper {
    internal lateinit var Settings: SharedPreferences

    internal fun showMessageDialog(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton("OK"
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

    internal fun showMessageDialog(context: Context, titleId: Int, messageId: Int) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(messageId)
                .setTitle(titleId)
                .setPositiveButton("OK"
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }
}