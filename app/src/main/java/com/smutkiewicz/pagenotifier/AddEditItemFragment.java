package com.smutkiewicz.pagenotifier;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.smutkiewicz.pagenotifier.database.DbDescription;

import static android.webkit.URLUtil.isValidUrl;

public class AddEditItemFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    // identyfikuje obiekt Loader
    private static final int WEBSITE_ITEMS_LOADER = 0;

    // zmienne instancyjne
    private boolean editMode = false;
    private Uri itemUri;

    // zmienne widoków
    private FrameLayout addEditFrameLayout;
    private EditText nameEditText;
    private EditText urlEditText;
    private TextView freqValueTextView;
    private Switch alertsSwitch;
    private SeekBar frequencySeekBar;
    private ProgressBar addEditProgressBar;
    private FloatingActionButton fab;

    private AddEditItemFragmentListener mListener;

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

    public interface AddEditItemFragmentListener {
        void onFragmentInteraction();
        void onAddEditItemCompleted(Uri contactUri);
    }

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

        setUpAddEditItemFab(view);
        setFormFillListener();
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
            int nameIndex = data.getColumnIndex(DbDescription.KEY_NAME);
            int urlIndex = data.getColumnIndex(DbDescription.KEY_URL);
            int alertsIndex = data.getColumnIndex(DbDescription.KEY_ALERTS);
            int freqIndex = data.getColumnIndex(DbDescription.KEY_DELAY);

            setNameAndUrlTextViews(data.getString(nameIndex), data.getString(urlIndex));
            setAlertsSwitchState(data.getInt(alertsIndex));
            setFrequencyStepValueLabelAndListener(data.getInt(freqIndex));
            hideLoaderProgressBar();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private void setUpAddEditItemFab(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.saveFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem();
            }
        });
    }

    private void setFormFillListener() {
        urlEditText.addTextChangedListener(new TextChangedListener());
    }

    private void getBundleArgumentsAndInitLoaderIfNeeded() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            itemUri = arguments.getParcelable(MainActivity.ITEM_URI);
        }

        if (itemUri != null)
            getLoaderManager().initLoader(WEBSITE_ITEMS_LOADER, null, this);
    }

    private void initNeededTypeOfView() {
        // zakładamy, że argumenty Bundle zostały pobrane
        if(itemUri != null)
            setEditModeView();
        else
            setNewItemModeView();
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

    private void deleteItemInEditMode() {
        // editmode = true
        // zaktualizuj informacje
        int updatedRows = getActivity().getContentResolver()
                .delete(itemUri, null, null);

        if (updatedRows > 0) {
            mListener.onAddEditItemCompleted(itemUri);
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
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbDescription.KEY_NAME,
                    nameEditText.getText().toString());
            contentValues.put(DbDescription.KEY_URL,
                    urlEditText.getText().toString());
            contentValues.put(DbDescription.KEY_ALERTS,
                    getAlertsSwitchState());
            contentValues.put(DbDescription.KEY_DELAY,
                    frequencySeekBar.getProgress());

            if (editMode) {
                // zaktualizuj informacje
                int updatedRows = getActivity().getContentResolver().update(
                        itemUri, contentValues, null, null);

                if (updatedRows > 0) {
                    mListener.onAddEditItemCompleted(itemUri);
                    showSnackbar(R.string.addedit_item_updated);
                }
                else {
                    showSnackbar(R.string.addedit_item_not_updated);
                }
            } else {
                Uri newItemUri = getActivity().getContentResolver().insert(
                        DbDescription.CONTENT_URI, contentValues);

                if (newItemUri != null) {
                    showSnackbar(R.string.addedit_new_item_added);
                    mListener.onAddEditItemCompleted(newItemUri);
                } else {
                    showSnackbar(R.string.addedit_new_item_not_added);
                }
            }
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
}
