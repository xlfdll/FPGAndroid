package xlfdll.nb.fpg;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up action bar
        Toolbar actionToolbar = (Toolbar) findViewById(R.id.actionToolbar);
        setSupportActionBar(actionToolbar);

        // Initialize settings
        AppHelper.Settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (AppHelper.Settings.getString(getString(R.string.pref_key_randomsalt), null) == null) {
            SharedPreferences.Editor prefEditor = AppHelper.Settings.edit();

            prefEditor.putString(
                    getString(R.string.pref_key_randomsalt),
                    PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                    .commit();

            AppHelper.showMessageDialog(this, R.string.alert_title_welcome, R.string.alert_message_firstrun);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Show last salt input
        EditText saltEditText = (EditText) findViewById(R.id.saltEditText);
        saltEditText.setText(AppHelper.Settings.getString(getString(R.string.pref_key_lastsalt), ""));
    }

    // Add menu items to action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_generate:
                EditText keywordEditText = (EditText) findViewById(R.id.keywordEditText);
                EditText saltEditText = (EditText) findViewById(R.id.saltEditText);
                TextView passwordTextView = (TextView) findViewById(R.id.passwordTextView);

                if (keywordEditText.getText().toString().isEmpty()) {
                    Snackbar.make(findViewById(R.id.mainLayout),
                            R.string.popup_keywordempty, Snackbar.LENGTH_SHORT)
                            .show();
                } else if (saltEditText.getText().toString().isEmpty()) {
                    Snackbar.make(findViewById(R.id.mainLayout),
                            R.string.popup_saltempty, Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    try {
                        passwordTextView.setText(
                                PasswordHelper.generatePassword(this,
                                        keywordEditText.getText().toString(),
                                        saltEditText.getText().toString(),
                                        Integer.parseInt(AppHelper.Settings.getString(
                                                getString(R.string.pref_key_length),
                                                Integer.toString(PasswordHelper.RandomSaltLength))))
                        );

                        // Auto copy?
                        if (AppHelper.Settings.getBoolean(getString(R.string.pref_key_rememberlastsalt), true)) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), passwordTextView.getText());
                            clipboard.setPrimaryClip(clip);
                        }

                        // Remember last salt input?
                        SharedPreferences.Editor editor = AppHelper.Settings.edit();

                        if (AppHelper.Settings.getBoolean(getString(R.string.pref_key_rememberlastsalt), true)) {
                            editor.putString(getString(R.string.pref_key_lastsalt), saltEditText.getText().toString())
                                    .commit();
                        } else {
                            editor.putString(getString(R.string.pref_key_lastsalt), "")
                                    .commit();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, OptionsActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}