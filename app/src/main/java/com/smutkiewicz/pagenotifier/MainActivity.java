package com.smutkiewicz.pagenotifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.Toast;

import com.smutkiewicz.pagenotifier.utilities.ScanDelayTranslator;

public class MainActivity extends AppCompatActivity
        implements AddEditItemFragment.AddEditItemFragmentListener,
        DetailsDialogFragment.DetailsDialogFragmentListener,
        MainActivityFragment.MainActivityFragmentListener,
        ServiceConnection {
    // klucz przeznaczony do przechowywania adresu Uri
    // w obiekcie przekazywanym do fragmentu
    public static final String ITEM_URI = "item_uri";
    public static ScanDelayTranslator scanDelayTranslator;

    private static Context context;
    private MainActivityFragment mainActivityFragment;
    private WebService service;
    private WebService.MyBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActivityToolbar();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        context = getApplicationContext();

        mainActivityFragment = new MainActivityFragment();
        addFragmentToContainerLayoutAndShowIt();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupPreferences();
        disableKeyboard();
        initScanDelayTranslator();

        startWebService();
    }

    private void startWebService() {
        Intent service = new Intent(getApplicationContext(), WebService.class);
        getApplicationContext().startService(service);

        /*Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);*/
    }

    @Override
    public void onServiceInteraction() {
        binder.updateServiceTask(1, 2, "Zadanie testowe 1",
                "https://developer.android.com/guide/components/services.html");
        binder.updateServiceTask(2, 3, "Zadanie testowe 2",
                "https://developer.android.com/guide/components/services.html");
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = (WebService.MyBinder) iBinder;
        service = ((WebService.MyBinder) iBinder).getService();
        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, WebValuesService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unbindService(this);
    }

    @Override
    public void displayAddEditFragment(Uri itemUri, int viewID) {
        AddEditItemFragment addEditFragment = new AddEditItemFragment();
        if(itemUri != Uri.EMPTY)
            addUriArgumentsToAFragment(addEditFragment, itemUri);

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void displayDetailsFragment(Uri itemUri, int viewId) {
        DetailsDialogFragment detailsDialog = new DetailsDialogFragment();
        addUriArgumentsToAFragment(detailsDialog, itemUri);
        detailsDialog.show(getSupportFragmentManager(), "Details fragment");
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    private void addUriArgumentsToAFragment(Fragment fragment, Uri uri) {
        // przekaż adres Uri jako argument fragmentu
        Bundle arguments = new Bundle();
        arguments.putParcelable(ITEM_URI, uri);
        fragment.setArguments(arguments);
    }

    @Override
    public void onItemDeleted() {
        getSupportFragmentManager().popBackStack();
        mainActivityFragment.updateWebsiteItemList();
    }

    @Override
    public void onGoToWebsite(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onChangesApplied() {
        mainActivityFragment.updateWebsiteItemList();
    }

    @Override
    public void onAddEditItemCompleted(Uri contactUri) {
        getSupportFragmentManager().popBackStack();
        mainActivityFragment.updateWebsiteItemList();
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

    private void disableKeyboard() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initScanDelayTranslator() {
        scanDelayTranslator = new ScanDelayTranslator(getApplicationContext());
    }

    @Override
    public void onFragmentInteraction() {
        //TODO implement interaction
    }
}
