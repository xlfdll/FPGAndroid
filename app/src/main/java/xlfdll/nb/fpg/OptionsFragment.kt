package xlfdll.nb.fpg

import android.app.Activity
import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.v7.app.AlertDialog
import android.widget.Toast

import java.io.File
import java.io.IOException

class OptionsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)

        if (isAdded) {
            AppHelper.Settings!!.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                val prefKey = getString(R.string.pref_key_randomsalt)

                if (key == prefKey) {
                    val randomSaltPreference = findPreference(prefKey) as EditTextPreference

                    randomSaltPreference.text = sharedPreferences.getString(key,
                            null)
                }
            }

            initializePreferenceButtons()
        }
    }

    fun initializePreferenceButtons() {
        var button = findPreference(getString(R.string.pref_key_randomsaltgenerate))

        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val activity = activity
            val builder = AlertDialog.Builder(activity)

            builder.setMessage(R.string.alert_message_randomsaltchange)
                    .setTitle(R.string.alert_title_warning)
                    .setPositiveButton(R.string.alert_button_yes
                    ) { dialog, whichButton ->
                        val prefEditor = AppHelper.Settings!!.edit()

                        prefEditor.putString(
                                getString(R.string.pref_key_randomsalt),
                                PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                                .apply()

                        dialog.dismiss()

                        Toast.makeText(activity.baseContext, R.string.popup_randomsaltgenerated, Toast.LENGTH_SHORT)
                                .show()
                    }
                    .setNegativeButton(R.string.alert_button_no
                    ) { dialog, whichButton -> dialog.dismiss() }
                    .create()
                    .show()

            true
        }

        button = findPreference(getString(R.string.pref_key_randomsaltbackup))

        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val activity = activity

            try {
                val file = PasswordHelper.getRandomSaltFile(activity)
                PasswordHelper.saveRandomSalt(file,
                        AppHelper.Settings!!.getString(getString(R.string.pref_key_randomsalt), null))

                // Force system to scan the new file in order to show in File Explorer on PC
                MediaScannerConnection.scanFile(activity,
                        arrayOf(file.absolutePath),
                        arrayOf(PasswordHelper.RandomSaltBackupDataMIMEType), null)

                Toast.makeText(activity.baseContext,
                        String.format(getString(R.string.popup_randomsaltsaved),
                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                        .show()

            } catch (e: IOException) {
                AppHelper.showMessageDialog(activity, "",
                        String.format(getString(R.string.popup_exception),
                                e.localizedMessage))
            }

            true
        }

        button = findPreference(getString(R.string.pref_key_randomsaltrestore))

        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val activity = activity
            val builder = AlertDialog.Builder(activity)

            val file = PasswordHelper.getRandomSaltFile(activity)

            if (file.exists()) {
                builder.setMessage(R.string.alert_message_randomsaltchange)
                        .setTitle(R.string.alert_title_warning)
                        .setPositiveButton(R.string.alert_button_yes
                        ) { dialog, whichButton ->
                            val prefEditor = AppHelper.Settings!!.edit()

                            try {
                                prefEditor.putString(
                                        getString(R.string.pref_key_randomsalt),
                                        PasswordHelper.loadRandomSalt(file))
                                        .apply()

                                Toast.makeText(activity.baseContext,
                                        String.format(getString(R.string.popup_randomsaltrestored),
                                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                                        .show()
                            } catch (e: IOException) {
                                AppHelper.showMessageDialog(activity, "",
                                        String.format(getString(R.string.popup_exception),
                                                e.localizedMessage))
                            }
                        }
                        .setNegativeButton("No"
                        ) { dialog, whichButton -> dialog.dismiss() }

            } else {
                Toast.makeText(activity.baseContext,
                        String.format(getString(R.string.alert_message_randomsaltfilenotfound),
                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                        .show()
            }

            builder.create().show()

            true
        }
    }
}