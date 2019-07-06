package xlfdll.nb.fpg

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

class OptionsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        // Set up action bar
        val actionToolbar = findViewById(R.id.actionToolbar) as Toolbar?
        setSupportActionBar(actionToolbar)

        setTitle(R.string.settings_title)

        fragmentManager
                .beginTransaction()
                .add(R.id.optionsLayout, OptionsFragment())
                .commit()
    }
}