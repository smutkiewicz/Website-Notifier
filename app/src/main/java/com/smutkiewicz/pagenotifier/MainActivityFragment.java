package com.smutkiewicz.pagenotifier;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.smutkiewicz.pagenotifier.database.DbDescription;
import com.smutkiewicz.pagenotifier.model.Website;
import com.smutkiewicz.pagenotifier.model.WebsiteItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    // identyfikuje obiekt Loader
    private static final int WEBSITE_ITEMS_LOADER = 0;

    //implementuje interfejs do komunikacji z MainActivity
    private MainActivityFragmentListener mListener;
    private boolean dialogOnScreen = false;
    private WebsiteItemAdapter itemAdapter;

    // interfejs do komunikacji z innymi fragmentami
    public interface MainActivityFragmentListener {
        void displayAddEditFragment(Uri uri, int viewId);
        void displayDetailsFragment(Uri itemUri, int viewId);
        void onGoToWebsite(String url);
        void onChangesApplied();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView =
                (RecyclerView) view.findViewById(R.id.recyclerView);
        setHasOptionsMenu(true);
        setUpAddItemFab(view);
        setUpRecyclerView(recyclerView);

        //makeSimpleDataInsertThemToDbAndShowInAdapter(setTestData());

        updateWebsiteItemList();

        return view;
    }

    private List<Website> setTestData() {
        List<Website> list = new ArrayList<>();
        list.add(new Website("Nowe zadanko",
                "https://github.com/smutkiewicz/Android-Soundbank/blob/master/app/src/main/" +
                        "java/com/smutkiewicz/soundbank/model/SoundArrayAdapter.java"));
        list.add(new Website("Poczta", "https://medusa.elka.pw.edu.pl/"));
        list.add(new Website("Mrow dyd", "http://www.if.pw.edu.pl/~mrow/dyd/"));
        list.add(new Website("Staty", "https://msoundtech.bandcamp.com/stats#zplays"));

        return list;
    }

    public void makeSimpleDataInsertThemToDbAndShowInAdapter(List<Website> list) {
        for(Website w : list) {
            Uri newItemUri = getActivity().getContentResolver().insert(
                    DbDescription.CONTENT_URI, w.getContentValues());
        }
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
        switch (id) {
            case WEBSITE_ITEMS_LOADER:
                return new CursorLoader(getActivity(),
                        DbDescription.CONTENT_URI, // adres Uri tabeli stron
                        null, // rzutowanie wartości null zwraca wszystkie kolumny
                        null, // wybranie wartości null zwraca wszystkie rzędy
                        null, // brak argumentów selekcji
                        DbDescription.KEY_ID + " COLLATE NOCASE ASC"); // kolejność sortowania
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        itemAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemAdapter.swapCursor(null);
    }

    private void setUpAddItemFab(View view) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO URI
                mListener.displayAddEditFragment(Uri.EMPTY, R.id.fragmentContainer);
            }
        });
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
                    public void onMoreButtonClick(Uri itemUri) {
                        mListener.displayDetailsFragment(itemUri, R.id.fragmentContainer);
                    }

                    @Override
                    public void onItemClick(String url) {
                        mListener.onGoToWebsite(url);
                    }

                    @Override
                    public void onSwitchClick(Uri itemUri, int newEnableValue) {
                        //TODO włącz/wyłącz powiadomienia
                        onSwitchClicked(itemUri, newEnableValue);
                    }
                }
        );
    }

    private void onSwitchClicked(Uri itemUri, int newIsEnabledValue) {
        // create ContentValues object containing contact's key-value pairs
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbDescription.KEY_ISENABLED, newIsEnabledValue);

        //int updatedRows = 0;
        int updatedRows = getActivity().getContentResolver().update(
                itemUri, contentValues, null, null);
        Log.d("TAG", "MainAFragment onSwitchClicked: "+itemUri);

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
