package org.xlfdll.nb.fpg

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.UnsupportedEncodingException
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up action_bar_main bar
        setSupportActionBar(findViewById(R.id.actionToolbar))

        // Initialize settings
        AppHelper.Settings = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (AppHelper.Settings.getString(getString(R.string.pref_key_random_salt), null) == null) {
            val prefEditor = AppHelper.Settings.edit()

            prefEditor.putString(
                    getString(R.string.pref_key_random_salt),
                    PasswordHelper.generateSalt(PasswordHelper.RandomSaltLength))
                    .commit()

            AppHelper.showMessageDialog(this, R.string.alert_title_welcome, R.string.alert_message_first_run)
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        // Show last salt input
        saltEditText.setText(AppHelper.Settings.getString(getString(R.string.pref_key_user_salt), ""))

        // Set EditText default actions
        keywordEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    generatePassword()

                    true
                }
                else -> false
            }
        }
    }

    // Add menu items to action_bar_main bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_generate -> {
                generatePassword()
            }
            R.id.action_settings -> {
                startActivity(Intent(this, OptionsActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun generatePassword() {
        when {
            keywordEditText!!.text.isEmpty() -> Toast.makeText(applicationContext,
                    R.string.popup_keyword_empty, Toast.LENGTH_SHORT)
                    .show()
            saltEditText!!.text.isEmpty() -> Toast.makeText(applicationContext,
                    R.string.popup_salt_empty, Toast.LENGTH_SHORT)
                    .show()
            else -> try {
                passwordTextView!!.text = PasswordHelper.generatePassword(this,
                        keywordEditText.text.toString(),
                        saltEditText.text.toString(),
                        Integer.parseInt(AppHelper.Settings.getString(
                                getString(R.string.pref_key_password_length),
                                PasswordHelper.RandomSaltLength.toString())!!))

                // Auto copy?
                if (AppHelper.Settings.getBoolean(getString(R.string.pref_key_auto_copy_password), true)) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(getString(R.string.app_name), passwordTextView.text)
                    clipboard.primaryClip = clip
                }

                // Remember last salt input?
                val editor = AppHelper.Settings.edit()

                if (AppHelper.Settings.getBoolean(getString(R.string.pref_key_remember_user_salt), true)) {
                    editor.putString(getString(R.string.pref_key_user_salt), saltEditText.text.toString())
                            .commit()
                } else {
                    editor.putString(getString(R.string.pref_key_user_salt), "")
                            .commit()
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }
    }
}