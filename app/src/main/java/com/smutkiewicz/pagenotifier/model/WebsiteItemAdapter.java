package com.smutkiewicz.pagenotifier.model;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smutkiewicz.pagenotifier.MainActivity;
import com.smutkiewicz.pagenotifier.R;
import com.smutkiewicz.pagenotifier.database.DbDescription;
import com.smutkiewicz.pagenotifier.service.Job;
import com.smutkiewicz.pagenotifier.service.JobFactory;

import static com.smutkiewicz.pagenotifier.MainActivity.ENABLED_ITEM_STATE;
import static com.smutkiewicz.pagenotifier.MainActivity.NOT_ENABLED_ITEM_STATE;

public class WebsiteItemAdapter
    extends RecyclerView.Adapter<WebsiteItemAdapter.ViewHolder> {
    // interfejs implementowany przez MainActivityFragment
    // po dotknięciu przez użytkownika elementu wyświetlanego w widoku RecyclerView
    public interface WebsiteItemClickListener {
        void onMoreButtonClick(Uri itemUri);
        void onItemClick(String url);
        void onToggleClick(Job job, boolean newToggleValue);
        void onSwitchClick(Uri itemUri, int newEnableValue);
    }

    // zmienne egzemplarzowe adaptera
    private Cursor cursor = null;
    private final WebsiteItemClickListener clickListener;

    public WebsiteItemAdapter(WebsiteItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // zagnieżdżona podklasa RecyclerView.ViewHolder używana do implementacji
    // wzorca ViewHolder w kontekście widoku RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView pageStateImageView;
        private final TextView pageNameTextView;
        private final ToggleButton isEnabledToggle;
        private final ImageButton pageMoreImageButton;

        private long rowID;
        private int delayStep;
        private boolean isEnabled;
        private String url;
        private Job job;

        // konfiguruje obiekt ViewHolder elementu widoku RecyclerView
        public ViewHolder(View itemView) {
            super(itemView);
            pageNameTextView = (TextView) itemView.findViewById(R.id.pageNameTextView);
            isEnabledToggle = (ToggleButton) itemView.findViewById(R.id.pageAlertToggle);
            pageMoreImageButton = (ImageButton) itemView.findViewById(R.id.pageMoreImageButton);
            pageStateImageView = (ImageView) itemView.findViewById(R.id.pageStateImageView);

            setButtonListeners();
            setItemViewListener();
            setIsEnabledToggleListener();
        }

        private void setButtonListeners() {
            pageMoreImageButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clickListener.onMoreButtonClick(
                                    DbDescription.buildWebsiteItemUri(rowID));
                        }
                    }
            );
        }

        private void setItemViewListener() {
            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clickListener.onItemClick(url);
                        }
                    }
            );
        }

        private void setIsEnabledToggleListener() {
            isEnabledToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //int newSwitchValue = swapBooleanToInt(isEnabled);
                    /*clickListener.onSwitchClick(
                            getItemUri(), newSwitchValue);*/
                    clickListener.onToggleClick(job, !isEnabled);
                }
            });
        }

        // określ identyfikator rzędu bazy danych strony
        // znajdującego się w tym obiekcie ViewHolder
        public void setRowID(long rowID) {
            this.rowID = rowID;
        }

        private void setJob() {
            JobFactory factory = new JobFactory();
            job = factory.produceJobFromACursor(cursor, getItemUri());
        }

        private Uri getItemUri() {
            return DbDescription.buildWebsiteItemUri(rowID);
        }
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    // zamień bieżący obiekt Cursor adaptera na nowy
    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    // ustala nowy element listy i jego obiekt ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    // określa tekst elementu listy w celu wyświetlenia etykiety zapytania
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.setRowID(cursor.getLong(cursor.getColumnIndex(DbDescription.KEY_ID)));
        holder.pageNameTextView.setText(cursor.getString(cursor.getColumnIndex(
                DbDescription.KEY_NAME)));
        holder.url = cursor.getString(cursor.getColumnIndex(DbDescription.KEY_URL));
        holder.setJob();

        setItemState(holder);
    }

    private void setItemState(ViewHolder holder) {
        int state =
                cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_ISENABLED));
        switch (state) {
            case NOT_ENABLED_ITEM_STATE:
                // OFF Pressed, not updated or already updated
                setNotEnabledState(holder);
                break;
            case ENABLED_ITEM_STATE:
                // ON Pressed, not updated
                setEnabledState(holder);
                break;
        }
    }

    // OFF Pressed, not updated or already updated
    private void setNotEnabledState(ViewHolder holder) {
        int isItemUpdated =
                cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_UPDATED));
        boolean isUpdated = (isItemUpdated == 1);
        setIsEnabledToggleCheck(holder, false);

        if(isUpdated)
            setUpdatedPageStateImage(holder);
        else
            setAbortedPageStateImage(holder);
    }

    // ON Pressed, not updated
    private void setEnabledState(ViewHolder holder) {
        setIsEnabledToggleCheck(holder, true);
        setNotUpdatedPageStateImage(holder);
    }

    private void setUpdatedPageStateImage(ViewHolder holder) {
        View view = holder.pageStateImageView.getRootView();
        holder.pageStateImageView.setImageDrawable(view.getResources()
                .getDrawable(R.drawable.ic_updated_black_24dp));
        holder.pageNameTextView.setTextColor(
                MainActivity.getAppContext().getResources().getColor(R.color.updated_green));
    }

    private void setNotUpdatedPageStateImage(ViewHolder holder) {
        View view = holder.pageStateImageView.getRootView();
        holder.pageStateImageView.setImageDrawable(view.getResources()
                .getDrawable(R.drawable.ic_not_updated_black_24dp));
        holder.pageNameTextView.setTextColor(
                MainActivity.getAppContext().getResources().getColor(R.color.secondary_text));
    }

    private void setAbortedPageStateImage(ViewHolder holder) {
        View view = holder.pageStateImageView.getRootView();
        holder.pageStateImageView.setImageDrawable(view.getResources()
                .getDrawable(R.drawable.ic_cancel_black_24dp));
        holder.pageNameTextView.setTextColor(
                MainActivity.getAppContext().getResources().getColor(R.color.secondary_text));
    }

    private void setIsEnabledToggleCheck(ViewHolder holder, boolean checked) {
        holder.isEnabledToggle.setChecked(checked);
        holder.isEnabled = checked;
    }

    private int swapBooleanToInt(boolean value) {
        return (value) ? 0 : 1;
    }
}
