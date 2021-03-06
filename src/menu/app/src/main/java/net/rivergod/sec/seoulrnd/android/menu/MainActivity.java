package net.rivergod.sec.seoulrnd.android.menu;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.Tracker;

import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        MenuApplication application = (MenuApplication) getApplication();
//        mTracker = application.getDefaultTracker();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Communicator.init(getApplicationContext());

        Communicator.getEventBus().register(this);

        onChangeDate(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(TAG, "Setting screen name: MainActivity");
//        mTracker.setScreenName("MainActivity");
//        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        Communicator.getEventBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onChangeDate(final Date targetDate) {
        Communicator.getMenu(null);
    }

    public void onEvent(DayCuisionsDTO e) {
        Log.d(TAG, "onEvent object e = " + e.getClass().getName() + ", value = " + e.toString());
//        mTracker.send(new HitBuilders.EventBuilder()
//                .setCategory("MainActivity")
//                .setAction("onEvent(e)")
//                .build());
    }

    public void onEvent(VolleyError error) {
        Log.d(TAG, "onEvent error = " + error.toString());
//        mTracker.send(new HitBuilders.EventBuilder()
//                .setCategory("MainActivity")
//                .setAction("onEvent(error)")
//                .build());

    }
}
