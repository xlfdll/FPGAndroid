package xlfdll.nb.fpg

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference.OnPreferenceClickListener

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

        button.onPreferenceClickListener = OnPreferenceClickListener {
            val currentContext = requireContext()
            val builder = AlertDialog.Builder(currentContext)

            builder.setMessage(R.string.alert_message_randomsaltchange)
                    .setTitle(R.string.alert_title_warning)
                    .setPositiveButton(R.string.alert_button_yes) { dialog, _ ->
                        val prefEditor = AppHelper.Settings!!.edit()

                        prefEditor.putString(
                                getString(R.string.pref_key_randomsalt),
                                PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                                .apply()

                        dialog.dismiss()

                        Toast.makeText(currentContext, R.string.popup_randomsaltgenerated, Toast.LENGTH_SHORT)
                                .show()
                    }
                    .setNegativeButton(R.string.alert_button_no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()

            true
        }

        button = findPreference(getString(R.string.pref_key_randomsaltbackup))

        button.onPreferenceClickListener = OnPreferenceClickListener {
            val currentContext = requireContext()

            if (ContextCompat.checkSelfPermission(currentContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                var builder = AlertDialog.Builder(currentContext)

                builder.setMessage(R.string.alert_message_writestoragepermissionrequest)
                        .setTitle(R.string.alert_title_permissiondenied)
                        .setPositiveButton("OK") { _, _ ->
                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                        }
                        .show()
            } else {
                try {
                    var directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

                    if (!directory.exists()) {
                        directory.mkdirs()
                    }

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
            }

            true
        }

        button = findPreference(getString(R.string.pref_key_randomsaltrestore))

        button.onPreferenceClickListener = OnPreferenceClickListener {
            val currentContext = requireContext()
            val builder = AlertDialog.Builder(currentContext)

            if (ContextCompat.checkSelfPermission(currentContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                builder.setMessage(R.string.alert_message_readstoragepermissionrequest)
                        .setTitle(R.string.alert_title_permissiondenied)
                        .setPositiveButton("OK") { _, _ ->
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                        }
                        .show()
            } else {
                val file = PasswordHelper.getRandomSaltFile(currentContext)

                if (file.exists()) {
                    builder.setMessage(R.string.alert_message_randomsaltchange)
                            .setTitle(R.string.alert_title_warning)
                            .setPositiveButton(R.string.alert_button_yes) { _, _ ->
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
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()

                } else {
                    AppHelper.showMessageDialog(currentContext, "",
                            String.format(getString(R.string.alert_message_randomsaltfilenotfound),
                                    PasswordHelper.RandomSaltBackupDataFileName))
                }
            }

            true
        }
    }
}