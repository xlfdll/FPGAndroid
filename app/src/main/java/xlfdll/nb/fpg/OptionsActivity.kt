package xlfdll.nb.fpg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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
}