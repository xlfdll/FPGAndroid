package org.xlfdll.nb.fpg

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.io.IOException

class OptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        // Set up action bar
        setSupportActionBar(findViewById(R.id.actionToolbar))

        setTitle(R.string.settings_title)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.optionsLayout, OptionsFragment())
                .commit()
    }

    class OptionsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.preferences, rootKey)

            initializePreferenceButtons()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key != null) {
                val prefKey = getString(R.string.pref_key_random_salt)

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
            findPreference(getString(R.string.pref_key_button_random_salt_generate)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val currentContext = requireContext()
                val builder = AlertDialog.Builder(currentContext)

                builder.setMessage(R.string.alert_message_random_salt_change)
                        .setTitle(R.string.alert_title_warning)
                        .setPositiveButton(R.string.alert_button_yes) { dialog, _ ->
                            val prefEditor = AppHelper.Settings.edit()

                            prefEditor.putString(
                                    getString(R.string.pref_key_random_salt),
                                    PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                                    .commit()

                            dialog.dismiss()

                            Toast.makeText(currentContext, R.string.popup_random_salt_generated, Toast.LENGTH_SHORT)
                                    .show()
                        }
                        .setNegativeButton(R.string.alert_button_no) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                true
            }

            findPreference(getString(R.string.pref_key_button_random_salt_backup)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val currentContext = requireContext()

                if (ContextCompat.checkSelfPermission(currentContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    val builder = AlertDialog.Builder(currentContext)

                    builder.setMessage(R.string.alert_message_write_storage_permission_request)
                            .setTitle(R.string.alert_title_permission_denied)
                            .setPositiveButton("OK") { _, _ ->
                                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                            }
                            .show()
                } else {
                    try {
                        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

                        if (!directory.exists()) {
                            directory.mkdirs()
                        }

                        val file = PasswordHelper.getRandomSaltFile(currentContext)

                        PasswordHelper.saveRandomSalt(file,
                                AppHelper.Settings.getString(getString(R.string.pref_key_random_salt), "")!!)

                        // Force system to scan the new file in order to show in File Explorer on PC
                        MediaScannerConnection.scanFile(activity,
                                arrayOf(file.absolutePath),
                                arrayOf(PasswordHelper.RandomSaltBackupDataMIMEType), null)

                        Toast.makeText(currentContext,
                                String.format(getString(R.string.popup_random_salt_saved),
                                        PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                                .show()

                    } catch (e: IOException) {
                        AppHelper.showMessageDialog(currentContext, "",
                                String.format(getString(R.string.popup_exception),
                                        e.localizedMessage))
                    }
                }

                true
            }

            findPreference(getString(R.string.pref_key_button_random_salt_restore)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val currentContext = requireContext()
                val builder = AlertDialog.Builder(currentContext)

                if (ContextCompat.checkSelfPermission(currentContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    builder.setMessage(R.string.alert_message_read_storage_permission_request)
                            .setTitle(R.string.alert_title_permission_denied)
                            .setPositiveButton("OK") { _, _ ->
                                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                            }
                            .show()
                } else {
                    val file = PasswordHelper.getRandomSaltFile(currentContext)

                    if (file.exists()) {
                        builder.setMessage(R.string.alert_message_random_salt_change)
                                .setTitle(R.string.alert_title_warning)
                                .setPositiveButton(R.string.alert_button_yes) { _, _ ->
                                    val prefEditor = AppHelper.Settings.edit()

                                    try {
                                        prefEditor.putString(
                                                getString(R.string.pref_key_random_salt),
                                                PasswordHelper.loadRandomSalt(file))
                                                .commit()

                                        Toast.makeText(currentContext,
                                                String.format(getString(R.string.popup_random_salt_restored),
                                                        PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                                                .show()
                                    } catch (e: IOException) {
                                        AppHelper.showMessageDialog(currentContext, "",
                                                String.format(getString(R.string.popup_exception),
                                                        e.localizedMessage))
                                    }
                                }
                                .setNegativeButton("No") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()

                    } else {
                        AppHelper.showMessageDialog(currentContext, "",
                                String.format(getString(R.string.alert_message_random_salt_file_not_found),
                                        PasswordHelper.RandomSaltBackupDataFileName))
                    }
                }

                true
            }
        }
    }
}