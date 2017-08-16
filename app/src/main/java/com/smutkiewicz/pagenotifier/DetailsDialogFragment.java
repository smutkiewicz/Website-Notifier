package com.smutkiewicz.pagenotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

public class DetailsDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        View detailsDialogView = getDetailsView();

        setBuildersViewAndTitle(builder, detailsDialogView);
        setBuildersNeutralGoToWebsiteButton(builder);
        setBuildersDeleteNegativeButton(builder);
        setBuildersEditPositiveButton(builder);

        return builder.create();
    }

    // zwróć odwołanie do DetailsDialog
    private View getDetailsView() {
        View detailsDialogView =
                getActivity().getLayoutInflater().inflate(
                        R.layout.fragment_details_dialog, null);
        return detailsDialogView;
    }

    // zwróć odwołanie do MainActivityFragment
    private MainActivityFragment getMainActivityFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(
                R.id.fragmentContainer);
    }

    private void setBuildersViewAndTitle(AlertDialog.Builder builder, View view) {
        builder.setView(view);
        builder.setTitle(R.string.details_title_label);
        builder.setIcon(R.drawable.ic_web_black_24dp);
    }

    private void setBuildersNeutralGoToWebsiteButton(AlertDialog.Builder builder) {
        builder.setNeutralButton(R.string.details_goto_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO goto
                    }
                }
        );
    }

    private void setBuildersDeleteNegativeButton(AlertDialog.Builder builder) {
        builder.setNegativeButton(R.string.details_delete_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO usuń
                    }
                }
        );
    }

    private void setBuildersEditPositiveButton(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.details_edit_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO edit
                    }
                }
        );
    }

    // poinformuj obiekt MainActivityFragment o tym,
    // że okno dialogowe jest właśnie wyświetlane
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivityFragment fragment = getMainActivityFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);
    }

    // poinformuj obiekt MainActivityFragment o tym,
    // że okno dialogowe nie jest już wyświetlane
    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getMainActivityFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(false);
    }
}
