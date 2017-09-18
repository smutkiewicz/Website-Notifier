package com.smutkiewicz.pagenotifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.smutkiewicz.pagenotifier.service.Job;
import com.smutkiewicz.pagenotifier.service.MyJobScheduler;
import com.smutkiewicz.pagenotifier.service.MyJobService;
import com.smutkiewicz.pagenotifier.utilities.PermissionGranter;
import com.smutkiewicz.pagenotifier.utilities.ScanDelayTranslator;

import java.lang.ref.WeakReference;

import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_ID_KEY;
import static com.smutkiewicz.pagenotifier.service.MyJobService.MESSENGER_INTENT_KEY;

public class MainActivity extends AppCompatActivity
        implements AddEditItemFragment.AddEditItemFragmentListener,
        DetailsDialogFragment.DetailsDialogFragmentListener,
        MainActivityFragment.MainActivityFragmentListener,
        FragmentManager.OnBackStackChangedListener {

    // komunikacja serwisu z aktywnością
    public static final int MSG_START = 0;
    public static final int MSG_STOP = 1;
    public static final int MSG_RESTART = 2;
    public static final int MSG_FINISHED = 3;
    public static final int MSG_FINISHED_WITH_ERROR = 4;

    // OFF Pressed, not updated or already updated
    public static final int NOT_ENABLED_ITEM_STATE = 0;
    // ON Pressed, not updated
    public static final int ENABLED_ITEM_STATE = 1;

    // klucz przeznaczony do przechowywania adresu Uri
    // w obiekcie przekazywanym do fragmentu
    public static final String ITEM_URI = "item_uri";

    // klasa narzędziowa
    public static ScanDelayTranslator scanDelayTranslator;

    private static Context context;
    private IncomingMessageHandler mHandler; // Handler na wiadomości od serwisu
    private ComponentName mServiceComponent;
    private MyJobScheduler mJobScheduler;
    private MainActivityFragment mainActivityFragment; // fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActivityToolbar();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        mainActivityFragment = new MainActivityFragment();
        context = getApplicationContext();
        addFragmentToContainerLayoutAndShowIt();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupPreferences();
        disableKeyboard();
        initScanDelayTranslator();
        setNavigationUpListener();

        mServiceComponent = new ComponentName(this, MyJobService.class);
        mHandler = new IncomingMessageHandler(this);
        mJobScheduler = new MyJobScheduler(getApplicationContext(),
                mServiceComponent, MainActivity.this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent startServiceIntent = new Intent(this, MyJobService.class);
        Messenger messengerIncoming = new Messenger(mHandler);
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
        startService(startServiceIntent);
    }

    @Override
    public void onStop() {
        stopService(new Intent(this, MyJobService.class));
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        boolean permissionsGranted = (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED);

        switch (requestCode) {
            case PermissionGranter.WRITE_READ_PERMISSIONS_FOR_ADD: {
                if(permissionsGranted)
                    displayAddEditFragment(Uri.EMPTY, R.id.fragmentContainer);
                else
                    showSnackbar(getString(R.string.main_granter_write_permission_denied));

                break;
            }
            case PermissionGranter.WRITE_READ_PERMISSIONS_FOR_EDIT: {
                if(!permissionsGranted) {
                    onChangesApplied();
                    showSnackbar(getString(R.string.main_granter_write_permission_denied));
                }
                break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp() {
        boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    @Override
    public void displayAddEditFragment(Uri itemUri, int viewID) {
        AddEditItemFragment addEditFragment = new AddEditItemFragment();
        if (itemUri != Uri.EMPTY)
            addUriArgumentsToAFragment(addEditFragment, itemUri);

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        hideAddItemFab();
    }

    @Override
    public void displayDetailsFragment(Uri itemUri, int viewId) {
        DetailsDialogFragment detailsDialog = new DetailsDialogFragment();
        addUriArgumentsToAFragment(detailsDialog, itemUri);
        detailsDialog.show(getSupportFragmentManager(), "Details fragment");
    }

    @Override
    public void onGoToWebsite(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onDeleteItemCompleted(int jobId) {
        returnToMainFragmentAndUpdateItemList();
        mJobScheduler.finishJob(jobId);
    }

    @Override
    public void onEditItemCompleted() {
        // po edycji nie wymuszamy od razu startu nowego zadania
        returnToMainFragmentAndUpdateItemList();
    }

    @Override
    public void onEditItemThatNeedsRestartingCompleted(Job job) {
        returnToMainFragmentAndUpdateItemList();
        showSnackbar("Restarting job " + job.id);
        mJobScheduler.resetJob(job);
    }

    @Override
    public void onAddItemCompleted(Job job) {
        // dodajemy nowe czyste zadanie i je uruchamiamy
        returnToMainFragmentAndUpdateItemList();
        mJobScheduler.scheduleJob(job);
    }

    @Override
    public void onToggleAction(Job job, boolean isSchedulingNeeded) {
        // obsługujemy akcję wciśnięcia toggle buttona przez użytkownika
        Log.d("Response", "on toggle");
        if(isSchedulingNeeded)
            mJobScheduler.scheduleJob(job);
        else
            mJobScheduler.finishJob(job.id);
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    public void onChangesApplied() {
        mainActivityFragment.updateWebsiteItemList();
    }

    private void setupActivityToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    // dodaj fragment do rozkładu FrameLayout
    private void addFragmentToContainerLayoutAndShowIt() {
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, mainActivityFragment);
        transaction.commit(); // wyświetl obiekt ContactsFragment
    }

    private void addUriArgumentsToAFragment(Fragment fragment, Uri uri) {
        // przekaż adres Uri jako argument fragmentu
        Bundle arguments = new Bundle();
        arguments.putParcelable(ITEM_URI, uri);
        fragment.setArguments(arguments);
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

    private void setNavigationUpListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        shouldDisplayHomeUp();
    }

    private void returnToMainFragmentAndUpdateItemList() {
        getSupportFragmentManager().popBackStack();
        mainActivityFragment.updateWebsiteItemList();
    }

    private void hideAddItemFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addFab);
        fab.hide();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.fragmentContainer),
                message, Toast.LENGTH_SHORT).show();
    }

    private static class IncomingMessageHandler extends Handler {
        // zapobiega problemom z WeakReference
        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super();
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                return;
            }

            int jobId = msg.getData().getInt(JOB_ID_KEY);

            switch (msg.what) {
                case MSG_START:
                    Log.d("Response MSG", "MSG_START for " + jobId);
                    break;
                case MSG_STOP:
                    Log.d("Response MSG", "MSG_STOP for " + jobId);
                    break;
                case MSG_RESTART:
                    Log.d("Response MSG", "MSG_RESTART for " + jobId);
                    break;
                case MSG_FINISHED:
                    Log.d("Response MSG", "MSG_FINISHED for " + jobId);
                    break;
                case MSG_FINISHED_WITH_ERROR:
                    Log.d("Response MSG", "MSG_FINISHED_WITH_ERROR " + jobId);
                    break;
            }
        }
    }
}
