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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.smutkiewicz.pagenotifier.service.Job;
import com.smutkiewicz.pagenotifier.service.JobFactory;
import com.smutkiewicz.pagenotifier.service.MyJobService;
import com.smutkiewicz.pagenotifier.utilities.ScanDelayTranslator;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AddEditItemFragment.AddEditItemFragmentListener,
        DetailsDialogFragment.DetailsDialogFragmentListener,
        MainActivityFragment.MainActivityFragmentListener {
    public static int mJobId = 0;

    // JobScheduler
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int MSG_START = 0;
    public static final int MSG_STOP = 1;

    public static final String MESSENGER_INTENT_KEY
            = BuildConfig.APPLICATION_ID + ".MESSENGER_INTENT_KEY";
    public static final String WORK_DURATION_KEY =
            BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY";
    public static final String JOB_NAME_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_NAME_KEY";
    public static final String JOB_URL_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_URL_KEY";

    // klucz przeznaczony do przechowywania adresu Uri
    // w obiekcie przekazywanym do fragmentu
    public static final String ITEM_URI = "item_uri";
    public static ScanDelayTranslator scanDelayTranslator;
    private static Context context;

    // Handler for incoming messages from the service.
    private IncomingMessageHandler mHandler;
    private ComponentName mServiceComponent;

    // fragment
    private MainActivityFragment mainActivityFragment;

    public static int id = 0;
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

        mServiceComponent = new ComponentName(this, MyJobService.class);
        mHandler = new IncomingMessageHandler(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent startServiceIntent = new Intent(this, MyJobService.class);
        Messenger messengerIncoming = new Messenger(mHandler);
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
        startService(startServiceIntent);
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, MyJobService.class));
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onServiceInteraction() {
        JobFactory factory = new JobFactory();
        Job job = factory.produceJob(JobFactory.getSampleJob());
        scheduleJob(job);
    }

    /**
     * Executed when user clicks on SCHEDULE JOB.
     */
    public void scheduleJob(Job job) {
        // sample values
        boolean requiresUnmetered = job.requiresUnmetered; // WiFi Connectivity
        boolean requiresAnyConnectivity = job.requiresAnyConnectivity; // Any Connectivity
        boolean requiresIdle = job.requiresIdle;
        boolean requiresCharging = job.requiresCharging;
        long delay = job.delay;
        long workDuration = job.workDuration;
        long deadline = job.deadline;
        int id = job.id;

        JobInfo.Builder builder = new JobInfo.Builder(id, mServiceComponent);
        builder.setMinimumLatency(job.delay);
        builder.setOverrideDeadline(job.deadline);

        if (requiresUnmetered) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else if (requiresAnyConnectivity) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        builder.setRequiresDeviceIdle(requiresIdle);
        builder.setRequiresCharging(requiresCharging);

        PersistableBundle extras = new PersistableBundle();
        extras.putLong(WORK_DURATION_KEY, workDuration);
        //do ustawienia powiadomienia
        extras.putString(JOB_NAME_KEY, job.name);
        extras.putString(JOB_URL_KEY, job.url);
        builder.setExtras(extras);

        Log.d(TAG, "Scheduling job");
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(builder.build());

        Toast.makeText(
                MainActivity.this, "Scheduling job", Toast.LENGTH_SHORT).show();
    }

    public void cancelAllJobs(View v) {
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancelAll();
        Toast.makeText(MainActivity.this, R.string.all_jobs_cancelled, Toast.LENGTH_SHORT).show();
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

    private static class IncomingMessageHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super(/* default looper */);
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
                    updateParamsTextView(msg.obj, "started");
                    break;
                case MSG_STOP:
                    updateParamsTextView(msg.obj, "stopped");
                    break;
            }
        }

        private void updateParamsTextView(@Nullable Object jobId, String action) {
            TextView paramsTextView = (TextView) mActivity.get().findViewById(R.id.taskParams);
            if (jobId == null) {
                paramsTextView.setText("");
                return;
            }
            String jobIdText = String.valueOf(jobId);
            paramsTextView.setText(String.format("Job ID %s %s", jobIdText, action));
        }
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
