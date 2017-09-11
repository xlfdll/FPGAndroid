package xlfdll.nb.fpg;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class OptionsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        if (isAdded()) {
            AppHelper.Settings.registerOnSharedPreferenceChangeListener(
                    new SharedPreferences.OnSharedPreferenceChangeListener() {
                        @Override
                        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                            String prefKey = getString(R.string.pref_key_randomsalt);

                            if (key.equals(prefKey)) {
                                EditTextPreference randomSaltPreference =
                                        (EditTextPreference) findPreference(prefKey);

                                randomSaltPreference.setText(sharedPreferences.getString(key, null));
                            }
                        }
                    }
            );

            initializePreferenceButtons();
        }
    }

    public void initializePreferenceButtons() {
        Preference button = findPreference(getString(R.string.pref_key_randomsaltgenerate));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Activity activity = getActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                builder.setMessage(R.string.alert_message_randomsaltchange)
                        .setTitle(R.string.alert_title_warning)
                        .setPositiveButton(R.string.alert_button_yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        SharedPreferences.Editor prefEditor = AppHelper.Settings.edit();

                                        prefEditor.putString(
                                                getString(R.string.pref_key_randomsalt),
                                                PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                                                .apply();

                                        dialog.dismiss();

                                        Toast.makeText(activity.getBaseContext(), R.string.popup_randomsaltgenerated, Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                        )
                        .setNegativeButton(R.string.alert_button_no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                }
                        )
                        .create()
                        .show();

                return true;
            }
        });

        button = findPreference(getString(R.string.pref_key_randomsaltbackup));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();

                try {
                    File file = PasswordHelper.getRandomSaltFile(activity);
                    PasswordHelper.saveRandomSalt(file,
                            AppHelper.Settings.getString(getString(R.string.pref_key_randomsalt), null));

                    // Force system to scan the new file in order to show in File Explorer on PC
                    MediaScannerConnection.scanFile(activity,
                            new String[]{file.getAbsolutePath()},
                            new String[]{PasswordHelper.RandomSaltBackupDataMIMEType},
                            null);

                    Toast.makeText(activity.getBaseContext(),
                            String.format(getString(R.string.popup_randomsaltsaved),
                                    PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                            .show();

                } catch (IOException e) {
                    AppHelper.showMessageDialog(activity, "",
                            String.format(getString(R.string.popup_exception),
                                    e.getLocalizedMessage()));
                }

                return true;
            }
        });

        button = findPreference(getString(R.string.pref_key_randomsaltrestore));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Activity activity = getActivity();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                final File file = PasswordHelper.getRandomSaltFile(activity);

                if (file.exists()) {
                    builder.setMessage(R.string.alert_message_randomsaltchange)
                            .setTitle(R.string.alert_title_warning)
                            .setPositiveButton(R.string.alert_button_yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            SharedPreferences.Editor prefEditor = AppHelper.Settings.edit();

                                            try {
                                                prefEditor.putString(
                                                        getString(R.string.pref_key_randomsalt),
                                                        PasswordHelper.loadRandomSalt(file))
                                                        .apply();

                                                Toast.makeText(activity.getBaseContext(),
                                                        String.format(getString(R.string.popup_randomsaltrestored),
                                                                PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                                                        .show();
                                            } catch (IOException e) {
                                                AppHelper.showMessageDialog(activity, "",
                                                        String.format(getString(R.string.popup_exception),
                                                                e.getLocalizedMessage()));
                                            }
                                        }
                                    }
                            )
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    }
                            );

                } else {
                    Toast.makeText(activity.getBaseContext(),
                            String.format(getString(R.string.alert_message_randomsaltfilenotfound),
                                    PasswordHelper.RandomSaltBackupDataFileName), Toast.LENGTH_SHORT)
                            .show();
                }

                builder.create().show();

                return true;
            }
        });
    }
}