package com.smutkiewicz.pagenotifier;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.smutkiewicz.pagenotifier.database.DbDescription;

import static com.smutkiewicz.pagenotifier.MainActivity.ENABLED_ITEM_STATE;
import static com.smutkiewicz.pagenotifier.MainActivity.NOT_ENABLED_ITEM_STATE;

public class DetailsDialogFragment extends DialogFragment
    implements LoaderManager.LoaderCallbacks<Cursor>{
    // identyfikuje obiekt Loader
    private static final int WEBSITE_ITEMS_LOADER = 0;

    // adres uri wyświetlanego itemu
    private Uri itemUri;
    private int itemId;
    private String url;
    private boolean isUpdatedStatus;
    private boolean isEnabledStatus;

    // pola do wyświetlania szczegółów obiektu
    private TextView nameTextView;
    private TextView urlTextView;
    private TextView frequencyTextView;
    private TextView alertsTextView;
    private TextView updatedTextView;

    private DetailsDialogFragmentListener mListener;
    private MyContentObserver mObserver;

    public interface DetailsDialogFragmentListener {
        void displayAddEditFragment(Uri itemUri, int viewId);
        void onDeleteItemCompleted(int jobId); // odświeża listę po zmianach w bazie danych
    }

    @SuppressLint("NewApi")
    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
            restartLoader();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            restartLoader();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        View detailsDialogView = inflateAndReturnDetailsView();

        findViewsAndAssignThem(detailsDialogView);
        setBuildersViewAndTitle(builder, detailsDialogView);
        setBuildersNeutralGoToWebsiteButton(builder);
        setBuildersDeleteNegativeButton(builder);
        setBuildersEditPositiveButton(builder);
        getBundleArguments();
        initLoader();
        //setBuildersIconAndStatusTextView(builder);
        initObserver();

        return builder.create();
    }

    // poinformuj obiekt MainActivityFragment o tym,
    // że okno dialogowe jest właśnie wyświetlane
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivityFragment fragment = getMainActivityFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);

        mListener = (DetailsDialogFragmentListener) context;
    }

    // poinformuj obiekt MainActivityFragment o tym,
    // że okno dialogowe nie jest już wyświetlane
    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getMainActivityFragment();
        getActivity().getContentResolver().
                unregisterContentObserver(mObserver);

        if (fragment != null)
            fragment.setDialogOnScreen(false);

        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;

        switch (id) {
            case WEBSITE_ITEMS_LOADER:
                cursorLoader = new CursorLoader(getActivity(),
                        itemUri, // adres Uri
                        null, // wszystkie kolumny
                        null, // wszystkie rzędy
                        null, // brak argumentów selekcji
                        null); // kolejność sortowania
                break;
            default:
                cursorLoader = null;
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int idIndex = data.getColumnIndex(DbDescription.KEY_ID);
            int nameIndex = data.getColumnIndex(DbDescription.KEY_NAME);
            int urlIndex = data.getColumnIndex(DbDescription.KEY_URL);
            int alertsIndex = data.getColumnIndex(DbDescription.KEY_ALERTS);
            int delayIndex = data.getColumnIndex(DbDescription.KEY_DELAY);
            int updatesIndex = data.getColumnIndex(DbDescription.KEY_UPDATED);
            int isEnabledIndex = data.getColumnIndex(DbDescription.KEY_ISENABLED);

            setItemId(data.getInt(idIndex));
            setDetailsNameAndUrlTextViews(data.getString(nameIndex), data.getString(urlIndex));
            setAlertModeState(data.getInt(alertsIndex));
            setStatus(data.getInt(updatesIndex));
            //setIsEnabledAndIsUpdated(data.getInt(isEnabledIndex), data.getInt(updatesIndex));
            setFrequencyStepLabel(data.getInt(delayIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    // zwróć odwołanie do DetailsDialog
    private View inflateAndReturnDetailsView() {
        View detailsDialogView =
                getActivity().getLayoutInflater().inflate(
                        R.layout.fragment_details_dialog, null);
        return detailsDialogView;
    }

    // zwróć odwołanie do MainActivityFragment
    private MainActivityFragment getMainActivityFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(
                R.id.fragmentMain);
    }

    private void setBuildersViewAndTitle(AlertDialog.Builder builder, View view) {
        builder.setView(view);
        builder.setTitle(R.string.details_title_label);
        builder.setIcon(R.drawable.ic_web_black_24dp);
    }

    private void setBuildersNeutralGoToWebsiteButton(AlertDialog.Builder builder) {
        builder.setNeutralButton(R.string.details_edit_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.displayAddEditFragment(itemUri, R.id.fragmentContainer);
                    }
                }
        );
    }

    private void setBuildersDeleteNegativeButton(AlertDialog.Builder builder) {
        builder.setNegativeButton(R.string.details_delete_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().getContentResolver().delete(
                                itemUri, null, null);
                        mListener.onDeleteItemCompleted(itemId);
                    }
                }
        );
    }

    private void setBuildersEditPositiveButton(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.details_ok_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );
    }

    private void findViewsAndAssignThem(View view) {
        nameTextView = view.findViewById(R.id.nameTextView);
        urlTextView = view.findViewById(R.id.urlTextView);
        frequencyTextView = view.findViewById(R.id.frequencyTextView);
        alertsTextView = view.findViewById(R.id.alertsTextView);
        updatedTextView = view.findViewById(R.id.updatedTextView);
    }

    private void getBundleArguments() {
        Bundle arguments = getArguments();
        if (arguments != null)
            itemUri = arguments.getParcelable(MainActivity.ITEM_URI);
    }

    private void initLoader() {
        getLoaderManager().initLoader(WEBSITE_ITEMS_LOADER, null, this);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(WEBSITE_ITEMS_LOADER, null, this);
    }

    private void initObserver() {
        mObserver = new MyContentObserver(new Handler());
        getActivity().getContentResolver().
                registerContentObserver(
                        itemUri,
                        true,
                        mObserver);
    }

    private void setBuildersIconAndStatusTextView(AlertDialog.Builder builder) {
        setItemState(builder);
    }

    private void setItemId(int id) {
        itemId = id;
    }

    private void setDetailsNameAndUrlTextViews(String name, String url) {
        this.url = url;
        nameTextView.setText(name);
        urlTextView.setText(url);
    }

    private void setAlertModeState(int areAlertsEnabled) {
        if(areAlertsEnabled == 1)
            alertsTextView.setText(R.string.details_alerts_on);
        else
            alertsTextView.setText(R.string.details_alerts_off);
    }

    private void setFrequencyStepLabel(int step) {
        String text =
                MainActivity.scanDelayTranslator.putStepAndReturnItsName(step);
        frequencyTextView.setText("Co " + text);
    }

    private void setStatus(int status) {
        // 1 == website updated
        if(status == 1) {
            updatedTextView.setText(R.string.details_updated);
            updatedTextView.setTextColor(Color.GREEN);
        } else {
            updatedTextView.setText(R.string.details_not_updated);
            updatedTextView.setTextColor(Color.RED);
        }

        isUpdatedStatus = (status == 1);
    }

    // TODO nowy sposób ustawiania stanu
    private void setIsEnabledAndIsUpdated(int isEnabled, int updates) {
        isEnabledStatus = (isEnabled == 1);
        isUpdatedStatus = (updates == 1);
        Log.d("LOADER_STATE", "isEnabled = " + isEnabled);
        if(isEnabledStatus) Log.d("LOADER_STATE", "true isEnabled = " + isEnabled);
        Log.d("LOADER_STATE", "updates = " + updates);
        if(isUpdatedStatus) Log.d("LOADER_STATE", "true updates = " + updates);
    }

    private void setItemState(AlertDialog.Builder builder) {
        int status = (isEnabledStatus) ? 1 : 0;
        Log.d("STATE", "status = " + status);

        switch (status) {
            case NOT_ENABLED_ITEM_STATE:
                // OFF Pressed, not updated or already updated
                Log.d("STATE", "NOT_ENABLED_ITEM_STATE");
                setNotEnabledState(builder);
                break;
            case ENABLED_ITEM_STATE:
                // ON Pressed, not updated
                Log.d("STATE", "ENABLED_ITEM_STATE");
                setEnabledState(builder);
                break;
        }
    }

    // OFF Pressed, not updated or already updated
    private void setNotEnabledState(AlertDialog.Builder builder) {
        if(isUpdatedStatus) {
            setUpdatedTextView();
            builder.setIcon(R.drawable.ic_updated_black_24dp);
        } else {
            setAbortedTextView();
            builder.setIcon(R.drawable.ic_cancel_black_24dp);
        }
    }

    // ON Pressed, not updated
    private void setEnabledState(AlertDialog.Builder builder) {
        setNotUpdatedTextView();
        builder.setIcon(R.drawable.ic_not_updated_black_24dp);
    }

    private void setNotUpdatedTextView() {
        updatedTextView.setText(R.string.details_not_updated);
        updatedTextView.setTextColor(Color.RED);
    }

    private void setUpdatedTextView() {
        updatedTextView.setText(R.string.details_updated);
        updatedTextView.setTextColor(Color.GREEN);
    }

    private void setAbortedTextView() {
        updatedTextView.setText(R.string.details_aborted);
        updatedTextView.setTextColor(Color.RED);
    }
}
