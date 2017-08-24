package com.smutkiewicz.pagenotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

    private boolean editMode = false;
    private Uri itemUri;

    private FrameLayout addEditFrameLayout;
    private EditText nameEditText;
    private EditText urlEditText;
    private TextView freqValueTextView;
    private Switch alertsSwitch;
    private SeekBar frequencySeekBar;
    private ProgressBar addEditProgressBar;

    private AddEditItemFragmentListener mListener;

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
        setHasOptionsMenu(true);
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
        getBundleArgumentsAndInitLoaderIfNeeded();

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
                //TODO delete item
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
            setFrequencyStepLabel(data.getInt(freqIndex));
            hideLoaderProgressBar();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private void getBundleArgumentsAndInitLoaderIfNeeded() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            editMode = true;
            itemUri = arguments.getParcelable(MainActivity.ITEM_URI);
        }

        if (itemUri != null)
            getLoaderManager().initLoader(WEBSITE_ITEMS_LOADER, null, this);
    }

    private void setUpAddEditItemFab(View view) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.saveFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem();
            }
        });
    }

    private void showLoaderProgressBar() {
        addEditProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoaderProgressBar() {
        addEditProgressBar.setVisibility(View.GONE);
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

    private void showSnackbar(int messageId) {
        Snackbar.make(addEditFrameLayout,
                messageId, Snackbar.LENGTH_LONG).show();
    }

    private int getAlertsSwitchState() {
        if(alertsSwitch.isChecked())
            return 1;
        else
            return 0;
    }

    private void setAlertsSwitchState(int checked) {
        if(checked == 1)
            alertsSwitch.setChecked(true);
        else //if checked == 0
            alertsSwitch.setChecked(false);
    }

    private void setFrequencyStepLabel(int delay) {
        //TODO DELAY
        frequencySeekBar.setProgress(1);
        freqValueTextView.setText("5 min");
    }

    private void setNameAndUrlTextViews(String name, String url) {
        nameEditText.setText(name);
        urlEditText.setText(url);
    }
}
