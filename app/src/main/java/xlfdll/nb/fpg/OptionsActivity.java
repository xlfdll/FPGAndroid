package xlfdll.nb.fpg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class OptionsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        // Set up action bar
        Toolbar actionToolbar = (Toolbar) findViewById(R.id.actionToolbar);
        setSupportActionBar(actionToolbar);

        setTitle(R.string.settings_title);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.optionsLayout, new OptionsFragment())
                .commit();
    }
}