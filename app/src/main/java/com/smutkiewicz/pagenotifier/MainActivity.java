package com.smutkiewicz.pagenotifier;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity
        implements AddEditItemFragment.AddEditItemFragmentListener {
    private MainActivityFragment mainActivityFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mainActivityFragment = new MainActivityFragment();

        // dodaj fragment do rozkładu FrameLayout
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mainActivityFragment);
        transaction.commit(); // wyświetl obiekt ContactsFragment

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    // wyświetl fragment umożliwiający dodanie nowego kontaktu lub edycję zapisanego wcześniej kontaktu
    private void displayAddEditFragment(int viewID) {
        AddEditItemFragment addEditFragment = new AddEditItemFragment();

        // skorzystaj z transakcji FragmentTransaction w celu wyświetlenia fragmentu AddEditFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // powoduje wyświetlenie fragmentu AddEditFragment
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO implement interaction
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            try {
                Intent preferencesIntent = new Intent(this, SettingsActivity.class);
                startActivity(preferencesIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        if(id == R.id.action_new) {
            //TODO action_new
            // skorzystaj z transakcji FragmentTransaction w celu wyświetlenia fragmentu AddEditFragment
            displayAddEditFragment(R.id.fragmentContainer);
        }

        return super.onOptionsItemSelected(item);
    }
}
