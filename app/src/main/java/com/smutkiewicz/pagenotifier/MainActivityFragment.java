package com.smutkiewicz.pagenotifier;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
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
        void displayAddEditFragment(int viewId);
        void displayDetailsFragment(Uri itemUri, int viewId);
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
        updateWebsiteItemList();
        //setTestData();
        return view;
    }

    /*private void setTestData() {
        ArrayList<Website> list = new ArrayList<>();
        list.add(new Website("Nowe zadanko",
                "https://github.com/smutkiewicz/Android-Soundbank/blob/master/app/src/main/" +
                        "java/com/smutkiewicz/soundbank/model/SoundArrayAdapter.java"));
        list.add(new Website("Poczta", "https://medusa.elka.pw.edu.pl/"));
        list.add(new Website("Mrow dyd", "http://www.if.pw.edu.pl/~mrow/dyd/"));
        list.add(new Website("Staty", "https://msoundtech.bandcamp.com/stats#zplays"));

        for(Website w : list) {
            Uri newItemUri = getActivity().getContentResolver().insert(
                    DbDescription.CONTENT_URI, w.getContentValues());
        }
    }*/

    private void setUpAddItemFab(View view) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.displayAddEditFragment(R.id.fragmentContainer);
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
                    public void onItemClick(Uri itemUri) {
                        //TODO wyślij usera do strony docelowej
                    }

                    @Override
                    public void onSwitchClick(Uri itemUri) {
                        //TODO włącz/wyłącz powiadomienia
                    }
                }
        );
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
}
