package xlfdll.nb.fpg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class OptionsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        // Set up action bar
        val actionToolbar = findViewById<Toolbar>(R.id.actionToolbar)
        setSupportActionBar(actionToolbar)

        setTitle(R.string.settings_title)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.optionsLayout, OptionsFragment())
                .commit()
    }
}