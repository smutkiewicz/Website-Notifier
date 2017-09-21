package com.smutkiewicz.pagenotifier;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.smutkiewicz.pagenotifier.database.DbDescription;
import com.smutkiewicz.pagenotifier.service.Job;
import com.smutkiewicz.pagenotifier.service.JobFactory;
import com.smutkiewicz.pagenotifier.utilities.InvalidJobUriException;

import static android.webkit.URLUtil.isValidUrl;
import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_URI_KEY;

public class AddEditItemFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // identyfikuje obiekt Loader
    private static final int WEBSITE_ITEMS_LOADER = 0;

    // zmienne instancyjne
    private boolean editMode = false;
    private Uri itemUri;
    private int itemId;

    // aktualny stan zadania ON/OFF
    private boolean isEnabled;

    // zmienne widoków
    private FrameLayout addEditFrameLayout;
    private EditText nameEditText;
    private EditText urlEditText;
    private TextView freqValueTextView;
    private Switch alertsSwitch;
    private SeekBar frequencySeekBar;
    private ProgressBar addEditProgressBar;
    private CheckBox saveBatteryCheckBox;
    private CheckBox networkCheckBox;
    private FloatingActionButton fab;

    // obserwator zmian w aktualnym zadaniu z identyfikatorem itemUri
    private MyContentObserver mObserver;
    private AddEditItemFragmentListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =
                inflater.inflate(R.layout.fragment_add_item, container, false);
        addEditFrameLayout = (FrameLayout) view.findViewById(R.id.addEditFrameLayout);
        nameEditText = (EditText) view.findViewById(R.id.nameEditText);
        urlEditText = (EditText) view.findViewById(R.id.urlEditText);
        freqValueTextView = (TextView) view.findViewById(R.id.freqValueTextView);
        alertsSwitch = (Switch) view.findViewById(R.id.alertsSwitch);
        frequencySeekBar = (SeekBar) view.findViewById(R.id.frequencySeekBar);
        addEditProgressBar = (ProgressBar) view.findViewById(R.id.addEditProgressBar);
        saveBatteryCheckBox = (CheckBox) view.findViewById(R.id.saveBatteryCheckBox);
        networkCheckBox = (CheckBox) view.findViewById(R.id.networkCheckBox);

        setUpAddEditItemFab(view);
        setFormFillListener();
        setCheckBoxLabels();
        getBundleArgumentsAndInitLoaderIfNeeded();
        initNeededTypeOfView();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add_edit_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                buildDeleteDialogAndConfirmDelete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (AddEditItemFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(editMode) {
            getActivity().getContentResolver().
                    unregisterContentObserver(mObserver);
            mObserver = null;
        }

        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showLoaderProgressBar();
        switch (id) {
            case WEBSITE_ITEMS_LOADER:
                return new CursorLoader(getActivity(),
                        itemUri,
                        null,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int idIndex = data.getColumnIndex(DbDescription.KEY_ID);
            int nameIndex = data.getColumnIndex(DbDescription.KEY_NAME);
            int urlIndex = data.getColumnIndex(DbDescription.KEY_URL);
            int alertsIndex = data.getColumnIndex(DbDescription.KEY_ALERTS);
            int freqIndex = data.getColumnIndex(DbDescription.KEY_DELAY);
            int isEnabledIndex = data.getColumnIndex(DbDescription.KEY_ISENABLED);
            int onlyWifiIndex = data.getColumnIndex(DbDescription.KEY_ONLY_WIFI);
            int saveBatteryIndex = data.getColumnIndex(DbDescription.KEY_SAVE_BATTERY);

            int isEnabledValue = data.getInt(isEnabledIndex);
            int onlyWifiValue = data.getInt(onlyWifiIndex);
            int saveBatteryValue = data.getInt(saveBatteryIndex);

            setItemId(data.getInt(idIndex));
            setNameAndUrlTextViews(data.getString(nameIndex), data.getString(urlIndex));
            setAlertsSwitchState(data.getInt(alertsIndex));
            setFrequencyStepValueLabelAndListener(data.getInt(freqIndex));
            setIsEnabled(isEnabledValue == 1);
            setSaveBatteryOptions(saveBatteryValue == 1);
            setNetworkOptions(onlyWifiValue == 1);
            hideLoaderProgressBar();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private void setUpAddEditItemFab(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.saveFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editMode) {
                    if(isEnabled)
                        buildEditDialogAndConfirmEdit();
                    else
                        saveItem();

                } else {
                    saveItem();
                }
            }
        });
    }

    private void setFormFillListener() {
        urlEditText.addTextChangedListener(new TextChangedListener());
    }

    private void setCheckBoxLabels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // inteligentne zarządzanie zadaniami w tle dla Android Oreo
            saveBatteryCheckBox.setText(R.string.addedit_save_battery_for_Oreo);
        } else {
            saveBatteryCheckBox.setText(R.string.addedit_save_battery);
        }
    }

    private void getBundleArgumentsAndInitLoaderIfNeeded() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            itemUri = arguments.getParcelable(MainActivity.ITEM_URI);
        }

        if (itemUri != null) {
            getLoaderManager().initLoader(WEBSITE_ITEMS_LOADER, null, this);
            initObserver();
        }
    }

    private void initNeededTypeOfView() {
        // zakładamy, że argumenty Bundle zostały pobrane
        if(itemUri != null)
            setEditModeView();
        else
            setNewItemModeView();
    }

    private void initObserver() {
        mObserver = new MyContentObserver(new Handler());
        getActivity().getContentResolver().
                registerContentObserver(
                        itemUri,
                        true,
                        mObserver);
    }

    private void setItemId(int id) {
        itemId = id;
    }

    private void setNameAndUrlTextViews(String name, String url) {
        nameEditText.setText(name);
        urlEditText.setText(url);
    }

    private void setAlertsSwitchState(int checked) {
        if(checked == 1)
            alertsSwitch.setChecked(true);
        else //if checked == 0
            alertsSwitch.setChecked(false);
    }

    private void setFrequencyStepValueLabelAndListener(int delayStep) {
        frequencySeekBar.setProgress(delayStep);
        freqValueTextView.setText(
                MainActivity.scanDelayTranslator.putStepAndReturnItsName(delayStep));
        setFrequencySeekBarListener();
    }

    private void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    private void setSaveBatteryOptions(boolean saveBattery) {
        saveBatteryCheckBox.setChecked(saveBattery);
    }

    private void setNetworkOptions(boolean onlyWifi) {
        networkCheckBox.setChecked(onlyWifi);
    }

    private void hideLoaderProgressBar() {
        addEditProgressBar.setVisibility(View.GONE);
    }

    private void setFrequencyStepLabel(int delayStep) {
        freqValueTextView.setText(
                MainActivity.scanDelayTranslator.putStepAndReturnItsName(delayStep));
    }

    private void setEditModeView() {
        fab.show();
        editMode = true;
        setHasOptionsMenu(true);
    }

    private void setNewItemModeView() {
        fab.hide();
        editMode = false;
        setHasOptionsMenu(false);
        addEditProgressBar.setVisibility(View.GONE);
        setFrequencyStepValueLabelAndListener(0);
    }

    private void buildDeleteDialogAndConfirmDelete() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.addedit_confirm_delete);
        builder.setMessage(R.string.addedit_message_confirm_delete);
        builder.setNegativeButton(R.string.addedit_dont_delete_button, null);
        builder.setPositiveButton(R.string.addedit_delete_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteItemInEditMode();
                    }
                });
        builder.create();
        builder.show();
    }

    private void buildEditDialogAndConfirmEdit() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.addedit_confirm_edit);
        builder.setMessage(R.string.addedit_message_confirm_edit);
        builder.setNegativeButton(R.string.addedit_dont_edit_button,
                new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onUserEscaped();
                }
        });
        builder.setPositiveButton(R.string.addedit_edit_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveItem();
                    }
                });
        builder.create();
        builder.show();
    }

    private void deleteItemInEditMode() {
        // editmode = true
        // zaktualizuj informacje
        int updatedRows = getActivity().getContentResolver()
                .delete(itemUri, null, null);

        if (updatedRows > 0) {
            mListener.onDeleteItemCompleted(itemId);
            showSnackbar(R.string.addedit_item_deleted);
        }
        else {
            showSnackbar(R.string.addedit_item_not_updated);
        }
    }

    private void showLoaderProgressBar() {
        addEditProgressBar.setVisibility(View.VISIBLE);
    }

    private void saveItem() {
        if(checkIfCorrect()) {
            JobFactory factory = new JobFactory();
            ContentValues contentValues = getContentValuesForSavingItemOperation();

            if (editMode) {
                // zaktualizuj informacje
                int updatedRows = getActivity().getContentResolver().update(
                        itemUri, contentValues, null, null);

                if (updatedRows > 0) {
                    // itemId został już pobrany w loaderze
                    if(isEnabled) {
                        contentValues.put(DbDescription.KEY_ID, itemId);
                        contentValues.put(JOB_URI_KEY, itemUri.toString());
                        Job job = factory.produceJob(contentValues);
                        mListener.onEditItemThatNeedsRestartingCompleted(job);
                    } else {
                        mListener.onEditItemCompleted();
                    }

                    showSnackbar(R.string.addedit_item_updated);
                }
                else {
                    showSnackbar(R.string.addedit_item_not_updated);
                }

            } else {
                // dodaj nowe informacje
                Uri newItemUri = getActivity().getContentResolver().insert(
                        DbDescription.CONTENT_URI, contentValues);

                if (newItemUri != null) {
                    try {
                        // zadanie zostało dopiero stworzone, więc musimy zdobyć
                        // autoinkrementowane przez bazę ID itemu
                        itemId = getIdOfJobFromAnUri(newItemUri);
                        contentValues.put(DbDescription.KEY_ID, itemId);
                        contentValues.put(JOB_URI_KEY, newItemUri.toString());
                        Job job = factory.produceJob(contentValues);
                        mListener.onAddItemCompleted(job);
                        showSnackbar(R.string.addedit_new_item_added);
                    } catch (InvalidJobUriException e) {
                        Log.d("TAG", e.getMessage());
                    }

                } else {
                    showSnackbar(R.string.addedit_new_item_not_added);
                }

            } // editMode
        } // checkIfCorrect();
    }

    private boolean checkIfCorrect() {
        String name = nameEditText.getText().toString();
        String url = urlEditText.getText().toString();

        if(name.length() == 0) {
            showSnackbar(R.string.addedit_no_name);
            return false; // no name input
        }

        if(url.length() == 0) {
            showSnackbar(R.string.addedit_no_url);
            return false; // no url
        } else {
            if(!isValidUrl(url)) {
                showSnackbar(R.string.addedit_incorrect_url);
                return false; //incorrect url
            }
        }
        return true;
    }

    private int getAlertsSwitchState() {
        if(alertsSwitch.isChecked())
            return 1;
        else
            return 0;
    }

    private int getCheckBoxState(CheckBox box) {
        if(box.isChecked())
            return 1;
        else
            return 0;
    }

    private void showSnackbar(int messageId) {
        Snackbar.make(addEditFrameLayout,
                messageId, Snackbar.LENGTH_LONG).show();
    }

    private void setFrequencySeekBarListener() {
        frequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setFrequencyStepLabel(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private boolean getIsEnabledValueOfJobFromAnUri(Uri itemUri) throws InvalidJobUriException {
        Cursor cursor = getActivity()
                .getContentResolver().query(itemUri, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            int isEnabledIndex = cursor.getColumnIndex(DbDescription.KEY_ISENABLED);
            int isEnabled = cursor.getInt(isEnabledIndex);
            return (isEnabled == 1);
        }

        throw new InvalidJobUriException("Invalid job Uri");
    }

    private int getIdOfJobFromAnUri(Uri itemUri) throws InvalidJobUriException {
        Cursor cursor = getActivity()
                .getContentResolver().query(itemUri, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DbDescription.KEY_ID);
            return cursor.getInt(idIndex);
        }

        throw new InvalidJobUriException("Invalid job Uri");
    }

    private ContentValues getContentValuesForSavingItemOperation() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbDescription.KEY_NAME,
                nameEditText.getText().toString());
        contentValues.put(DbDescription.KEY_URL,
                urlEditText.getText().toString());
        contentValues.put(DbDescription.KEY_ALERTS,
                getAlertsSwitchState());
        contentValues.put(DbDescription.KEY_DELAY,
                frequencySeekBar.getProgress());
        contentValues.put(DbDescription.KEY_SAVE_BATTERY,
                getCheckBoxState(saveBatteryCheckBox));
        contentValues.put(DbDescription.KEY_ONLY_WIFI,
                getCheckBoxState(networkCheckBox));
        return contentValues;
    }

    public interface AddEditItemFragmentListener {
        void onAddItemCompleted(Job job);
        void onEditItemCompleted();
        void onEditItemThatNeedsRestartingCompleted(Job job);
        void onDeleteItemCompleted(int jobId);
        void onUserEscaped();
    }

    private class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() != 0)
                fab.show();
            else
                fab.hide();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    @SuppressLint("NewApi")
    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
            try {
                boolean value =
                        getIsEnabledValueOfJobFromAnUri(itemUri);
                setIsEnabled(value);
            } catch (InvalidJobUriException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) { }
    }
}
