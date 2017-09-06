package com.smutkiewicz.pagenotifier;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.smutkiewicz.pagenotifier.database.DbDescription;
import com.smutkiewicz.pagenotifier.model.WebsiteItemAdapter;
import com.smutkiewicz.pagenotifier.service.Job;
import com.smutkiewicz.pagenotifier.utilities.ItemDivider;

public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    // identyfikuje obiekt Loader
    private static final int WEBSITE_ITEMS_LOADER = 0;

    //implementuje interfejs do komunikacji z MainActivity
    private MainActivityFragmentListener mListener;
    private boolean dialogOnScreen = false;
    private WebsiteItemAdapter itemAdapter;

    private EditText searchEditText;
    private FloatingActionButton fab;

    // interfejs do komunikacji z innymi fragmentami
    public interface MainActivityFragmentListener {
        void displayAddEditFragment(Uri uri, int viewId);
        void displayDetailsFragment(Uri itemUri, int viewId);
        void onToggleAction(Job job, boolean isSchedulingNeeded);
    }

    private class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            performSearchQuery();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView =
                (RecyclerView) view.findViewById(R.id.recyclerView);
        searchEditText = (EditText) view.findViewById(R.id.searchEditText);

        setHasOptionsMenu(true);
        setUpAddItemFab(view);
        setUpRecyclerView(recyclerView);
        setUpInputTextLayout();
        updateWebsiteItemList();

        return view;
    }

    private void setUpInputTextLayout() {
        searchEditText.addTextChangedListener(new TextChangedListener());
    }

    public void setDialogOnScreen(boolean visible) {
        dialogOnScreen = visible;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainActivityFragment.MainActivityFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        hideAddItemFab();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                try {
                    Intent preferencesIntent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(preferencesIntent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWebsiteItemList() {
        itemAdapter.notifyDataSetChanged();
    }

    // inicjuj obiekt Loader po utworzeniu aktywności tego fragmentu
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(WEBSITE_ITEMS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //TODO CursorLoader
        String where = buildWhereClause();
        if(where != null)
            Log.d("TAG", where);

        switch (id) {
            case WEBSITE_ITEMS_LOADER:
                return new CursorLoader(getActivity(),
                        DbDescription.CONTENT_URI, // adres Uri tabeli stron
                        null,
                        where,
                        null,
                        DbDescription.KEY_ID + " COLLATE NOCASE ASC"); // kolejność sortowania
            default:
                return null;
        }
    }

    private String buildWhereClause() {
        if(searchEditText != null)
            return DbDescription.KEY_NAME +
                    " LIKE '%" + searchEditText.getText().toString() + "%'";
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        itemAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemAdapter.swapCursor(null);
    }

    private void performSearchQuery() {
        getLoaderManager().restartLoader(WEBSITE_ITEMS_LOADER, null, this);
        updateWebsiteItemList();
    }

    private void setUpAddItemFab(View view) {
        fab = (FloatingActionButton) getActivity().findViewById(R.id.addFab);
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.displayAddEditFragment(Uri.EMPTY, R.id.fragmentContainer);
                hideAddItemFab();
            }
        });
    }

    private void hideAddItemFab() {
        fab.hide();
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
        // wyświetlanie elementów w formie pionowej listy
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity().getBaseContext()));
        implementAdapterItemClickListener();
        recyclerView.setAdapter(itemAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    private void implementAdapterItemClickListener() {
        itemAdapter = new WebsiteItemAdapter(
                new WebsiteItemAdapter.WebsiteItemClickListener() {
                    @Override
                    public void onEditButtonClick(Uri itemUri, int viewId) {
                        mListener.displayAddEditFragment(itemUri, viewId);
                    }

                    @Override
                    public void onItemClick(Uri itemUri) {
                        mListener.displayDetailsFragment(itemUri, R.id.fragmentContainer);
                    }

                    @Override
                    public void onToggleClick(Job job, boolean newToggleValue) {
                        onToggleClicked(job.uri, (newToggleValue) ? 1 : 0);
                        mListener.onToggleAction(job, newToggleValue);
                    }
                }
        );
    }

    private void onToggleClicked(Uri itemUri, int newIsEnabledValue) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbDescription.KEY_ISENABLED, newIsEnabledValue);
        contentValues.put(DbDescription.KEY_UPDATED, 0);

        int updatedRows = getActivity().getContentResolver().update(
                itemUri, contentValues, null, null);

        if (updatedRows > 0) {

            if(newIsEnabledValue == 1) {
                Snackbar.make(getActivity().findViewById(R.id.fragmentContainer),
                        R.string.main_enabled, Snackbar.LENGTH_LONG).show();
            } else { // isEnabled == 0
                Snackbar.make(getActivity().findViewById(R.id.fragmentContainer),
                        R.string.main_disabled, Snackbar.LENGTH_LONG).show();
            }

        } else {
            Snackbar.make(getActivity().findViewById(R.id.fragmentContainer),
                    "Błąd", Snackbar.LENGTH_LONG).show();
        }
    }
}
