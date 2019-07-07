package xlfdll.nb.fpg

import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.appcompat.app.AlertDialog

import java.io.IOException

class OptionsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initializePreferenceButtons()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            val prefKey = getString(R.string.pref_key_randomsalt)

            when (key) {
                prefKey -> {
                    val randomSaltPreference = findPreference(prefKey) as EditTextPreference

                    randomSaltPreference.text = sharedPreferences?.getString(key, null)
                }
            }
        }
    }

    override fun onResume() {
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        super.onResume()
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onPause()
    }

    // Need to use requireContext()
    private fun initializePreferenceButtons() {
        var button = findPreference(getString(R.string.pref_key_randomsaltgenerate))

        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val currentContext = requireContext()
            val builder = AlertDialog.Builder(currentContext)

            builder.setMessage(R.string.alert_message_randomsaltchange)
                    .setTitle(R.string.alert_title_warning)
                    .setPositiveButton(R.string.alert_button_yes
                    ) { dialog, _ ->
                        val prefEditor = AppHelper.Settings!!.edit()

                        prefEditor.putString(
                                getString(R.string.pref_key_randomsalt),
                                PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                                .apply()

                        dialog.dismiss()

                        Toast.makeText(currentContext, R.string.popup_randomsaltgenerated, Toast.LENGTH_SHORT)
                                .show()
                    }
                    .setNegativeButton(R.string.alert_button_no
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()

            true
        }

        button = findPreference(getString(R.string.pref_key_randomsaltbackup))

        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val currentContext = requireContext()

            try {
                val file = PasswordHelper.getRandomSaltFile(currentContext)
                PasswordHelper.saveRandomSalt(file,
                        AppHelper.Settings!!.getString(getString(R.string.pref_key_randomsalt), "")!!)

                // Force system to scan the new file in order to show in File Explorer on PC
                MediaScannerConnection.scanFile(activity,
                        arrayOf(file.absolutePath),
                        arrayOf(PasswordHelper.RandomSaltBackupDataMIMEType), null)

                Toast.makeText(currentContext,
                        String.format(getString(R.string.popup_randomsaltsaved),
                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                        .show()

            } catch (e: IOException) {
                AppHelper.showMessageDialog(currentContext, "",
                        String.format(getString(R.string.popup_exception),
                                e.localizedMessage))
            }

            true
        }

        button = findPreference(getString(R.string.pref_key_randomsaltrestore))

        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val currentContext = requireContext()
            val builder = AlertDialog.Builder(currentContext)

            val file = PasswordHelper.getRandomSaltFile(currentContext)

            if (file.exists()) {
                builder.setMessage(R.string.alert_message_randomsaltchange)
                        .setTitle(R.string.alert_title_warning)
                        .setPositiveButton(R.string.alert_button_yes
                        ) { _, _ ->
                            val prefEditor = AppHelper.Settings!!.edit()

                            try {
                                prefEditor.putString(
                                        getString(R.string.pref_key_randomsalt),
                                        PasswordHelper.loadRandomSalt(file))
                                        .apply()

                                Toast.makeText(currentContext,
                                        String.format(getString(R.string.popup_randomsaltrestored),
                                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                                        .show()
                            } catch (e: IOException) {
                                AppHelper.showMessageDialog(currentContext, "",
                                        String.format(getString(R.string.popup_exception),
                                                e.localizedMessage))
                            }
                        }
                        .setNegativeButton("No"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }

            } else {
                Toast.makeText(currentContext,
                        String.format(getString(R.string.alert_message_randomsaltfilenotfound),
                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                        .show()
            }

            builder.create().show()

            true
        }
    }
}