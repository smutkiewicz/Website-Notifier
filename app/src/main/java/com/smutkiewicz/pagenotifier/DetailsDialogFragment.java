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
import android.view.View;
import android.widget.TextView;

import com.smutkiewicz.pagenotifier.database.DbDescription;

public class DetailsDialogFragment extends DialogFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    // identyfikuje obiekt Loader
    private static final int WEBSITE_ITEMS_LOADER = 0;

    // adres uri wyświetlanego itemu
    private Uri itemUri;
    private int itemId;
    private String url;

    // pola do wyświetlania szczegółów obiektu
    private TextView nameTextView;
    private TextView urlTextView;
    private TextView frequencyTextView;
    private TextView alertsTextView;
    private TextView updatedTextView;

    private Dialog mDialog;
    private DetailsDialogFragmentListener mListener;
    private MyContentObserver mObserver;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        View detailsDialogView = inflateAndReturnDetailsView();

        findViewsAndAssignThem(detailsDialogView);
        setBuildersViewAndTitle(builder, detailsDialogView);
        setBuildersNegativeGoToWebsiteButton(builder);
        setBuildersEditPositiveButton(builder);
        getBundleArguments();
        initLoader();
        initObserver();

        mDialog = builder.create();
        return mDialog;
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
            setStatus(data.getInt(updatesIndex), data.getInt(isEnabledIndex));
            setFrequencyStepLabel(data.getInt(delayIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    // zwróć odwołanie do DetailsDialog
    private View inflateAndReturnDetailsView() {
        return getActivity().getLayoutInflater().inflate(
                        R.layout.fragment_details_dialog, null);
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

    private void setBuildersNegativeGoToWebsiteButton(AlertDialog.Builder builder) {
        builder.setNegativeButton(R.string.details_goto_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onGoToWebsite(url);
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
        frequencyTextView.setText(getString(R.string.details_co) + " " + text);
    }

    private void setStatus(int isUpdated, int isEnabled) {
        if(isEnabled == 0 && isUpdated == 1) {
            updatedTextView.setText(R.string.details_updated);
            updatedTextView.setTextColor(Color.GREEN);
        }

        if(isEnabled == 0 && isUpdated == 0) {
            updatedTextView.setText(R.string.details_aborted);
            updatedTextView.setTextColor(Color.RED);
        }

        if(isEnabled == 1 && isUpdated == 0) {
            updatedTextView.setText(R.string.details_not_updated);
            updatedTextView.setTextColor(Color.RED);
        }
    }

    public interface DetailsDialogFragmentListener {
        void onDeleteItemCompleted(int jobId); // odświeża listę po zmianach w bazie danych
        void onGoToWebsite(String url);
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
}