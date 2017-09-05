package com.smutkiewicz.pagenotifier;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.smutkiewicz.pagenotifier.service.Job;
import com.smutkiewicz.pagenotifier.service.MyJobService;
import com.smutkiewicz.pagenotifier.utilities.ScanDelayTranslator;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AddEditItemFragment.AddEditItemFragmentListener,
        DetailsDialogFragment.DetailsDialogFragmentListener,
        MainActivityFragment.MainActivityFragmentListener {

    public static final int MSG_START = 0;
    public static final int MSG_STOP = 1;

    // OFF Pressed, not updated or already updated
    public static final int NOT_ENABLED_ITEM_STATE = 0;
    // ON Pressed, not updated
    public static final int ENABLED_ITEM_STATE = 1;

    // zestaw stałych do obsługi dołączania Extras dla serwisu
    public static final String MESSENGER_INTENT_KEY
            = BuildConfig.APPLICATION_ID + ".MESSENGER_INTENT_KEY";
    public static final String WORK_DURATION_KEY =
            BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY";
    public static final String JOB_NAME_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_NAME_KEY";
    public static final String JOB_URL_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_URL_KEY";
    public static final String JOB_ALERTS_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_ALERTS_KEY";
    public static final String JOB_URI_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_URI_KEY";

    // klucz przeznaczony do przechowywania adresu Uri
    // w obiekcie przekazywanym do fragmentu
    public static final String ITEM_URI = "item_uri";

    // klasa narzędziowa
    public static ScanDelayTranslator scanDelayTranslator;

    // JobScheduler
    private static final String TAG = MainActivity.class.getSimpleName();
    private static Context context;

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

            Message m;
            switch (msg.what) {
                case MSG_START:
                    // TODO REAKCJA NA START JOB-U W SERWISIE
                    break;
                case MSG_STOP:
                    // TODO REAKCJA NA STOP JOB-U W SERWISIE
                    break;
            }
        }
    }

    // Handler na wiadomości od serwisu
    private IncomingMessageHandler mHandler;
    private ComponentName mServiceComponent;

    // fragment
    private MainActivityFragment mainActivityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        mServiceComponent = new ComponentName(this, MyJobService.class);
        mHandler = new IncomingMessageHandler(this);
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

    public void scheduleJob(Job job) {
        // sample values
        boolean requiresUnmetered = job.requiresUnmetered; // WiFi Connectivity
        boolean requiresAnyConnectivity = job.requiresAnyConnectivity; // Any Connectivity

        // build Job for JobService
        JobInfo.Builder builder = new JobInfo.Builder(job.id, mServiceComponent);
        builder.setMinimumLatency(job.delay);
        builder.setOverrideDeadline(job.deadline);
        builder.setRequiresDeviceIdle(job.requiresIdle);
        builder.setRequiresCharging(job.requiresCharging);

        if (requiresUnmetered) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else if (requiresAnyConnectivity) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        // put extras for service
        PersistableBundle extras = putExtrasToAPersistableBundle(job);
        builder.setExtras(extras);

        // schedule
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(builder.build());

        Log.d(TAG, "Scheduling job");
        Toast.makeText(
                MainActivity.this, "Scheduling job", Toast.LENGTH_SHORT).show();
    }

    public void cancelAllJobs(View v) {
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
        Log.d(TAG, "Cancelling all jobs");
        Toast.makeText(MainActivity.this, R.string.all_jobs_cancelled,
                Toast.LENGTH_SHORT).show();
    }

    public void finishJob(int jobId) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
        if (allPendingJobs.size() > 0) {
            jobScheduler.cancel(jobId);
            Toast.makeText(
                    MainActivity.this, String.format(getString(R.string.cancelled_job), jobId),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(
                    MainActivity.this, getString(R.string.no_jobs_to_cancel),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void restartJob(Job job) {
        finishJob(job.id);
        scheduleJob(job);
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

    @Override
    public void onGoToWebsite(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onDeleteItemCompleted(int jobId) {
        returnToMainFragmentAndUpdateItemList();
        finishJob(jobId);
    }

    @Override
    public void onEditItemCompleted() {
        // po edycji nie wymuszamy od razu startu nowego zadania
        returnToMainFragmentAndUpdateItemList();
    }

    @Override
    public void onEditItemThatNeedsRestartingCompleted(Job job) {
        returnToMainFragmentAndUpdateItemList();
        Snackbar.make(findViewById(R.id.fragmentContainer),
                "Restarting job " + job.id, Toast.LENGTH_SHORT).show();
        restartJob(job);
    }

    @Override
    public void onAddItemCompleted(Job job) {
        // dodajemy nowe czyste zadanie i je uruchamiamy
        returnToMainFragmentAndUpdateItemList();
        scheduleJob(job);
    }

    @Override
    public void onToggleAction(Job job, boolean isSchedulingNeeded) {
        // obsługujemy akcję wciśnięcia toggle buttona przez użytkownika
        if(isSchedulingNeeded)
            scheduleJob(job);
        else
            finishJob(job.id);
    }

    public void onChangesApplied() {
        mainActivityFragment.updateWebsiteItemList();
    }

    public static Context getAppContext() {
        return MainActivity.context;
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

    private PersistableBundle putExtrasToAPersistableBundle(Job job) {
        PersistableBundle extras = new PersistableBundle();
        extras.putLong(WORK_DURATION_KEY, job.workDuration);
        //do ustawienia powiadomienia
        extras.putString(JOB_NAME_KEY, job.name);
        extras.putString(JOB_URL_KEY, job.url);
        extras.putInt(JOB_ALERTS_KEY, (job.alertsEnabled) ? 1 : 0);
        extras.putString(JOB_URI_KEY, (job.uri).toString());
        return extras;
    }

    private void returnToMainFragmentAndUpdateItemList() {
        getSupportFragmentManager().popBackStack();
        mainActivityFragment.updateWebsiteItemList();
    }
}
