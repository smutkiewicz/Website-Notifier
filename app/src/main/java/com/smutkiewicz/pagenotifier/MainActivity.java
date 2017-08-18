package com.smutkiewicz.pagenotifier;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.smutkiewicz.pagenotifier.database.DbDescription;
import com.smutkiewicz.pagenotifier.model.Website;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements AddEditItemFragment.AddEditItemFragmentListener,
        MainActivityFragment.MainActivityFragmentListener{

    private MainActivityFragment mainActivityFragment;

    // klucz przeznaczony do przechowywania adresu Uri
    // w obiekcie przekazywanym do fragmentu
    public static final String ITEM_URI = "item_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActivityToolbar();

        mainActivityFragment = new MainActivityFragment();
        addFragmentToContainerLayoutAndShowIt();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupPreferences();
        //setTestData();
    }

    @Override
    public void displayAddEditFragment(int viewID) {
        AddEditItemFragment addEditFragment = new AddEditItemFragment();
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void displayDetailsFragment(Uri itemUri, int viewId) {
        DetailsDialogFragment detailsDialog = new DetailsDialogFragment();

        // przekaż adres Uri kontaktu jako argument fragmentu DetailsDialogFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(ITEM_URI, itemUri);
        detailsDialog.setArguments(arguments);

        detailsDialog.show(getSupportFragmentManager(), "Details fragment");
    }

    private void setupActivityToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // dodaj fragment do rozkładu FrameLayout
    private void addFragmentToContainerLayoutAndShowIt() {
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mainActivityFragment);
        transaction.commit(); // wyświetl obiekt ContactsFragment
    }

    //SharedPreferences
    private void setupPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public void onFragmentInteraction() {
        //TODO implement interaction
    }
}
