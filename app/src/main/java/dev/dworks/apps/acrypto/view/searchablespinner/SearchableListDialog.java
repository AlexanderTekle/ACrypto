package dev.dworks.apps.acrypto.view.searchablespinner;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.List;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.DialogFragment;

public class SearchableListDialog extends DialogFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "SearchableListDialog";
    private static final String ITEMS = "items";
    private static final String TITLE = "title";

    private ArrayAdapter listAdapter;
    private ListView _listViewItems;
    private SearchableItem _searchableItem;
    private OnSearchTextChanged _onSearchTextChanged;
    private SearchView _searchView;
    private String _strPositiveButtonText;
    private DialogInterface.OnClickListener _onClickListener;
    private List mItems;
    private String mTitle;

    public SearchableListDialog() {

    }

    public static SearchableListDialog show(FragmentManager fm, List items, String title) {
        final SearchableListDialog dialog = new SearchableListDialog();
        final Bundle args = new Bundle();
        args.putSerializable(ITEMS,  (Serializable) items);
        args.putString(TITLE, title);
        dialog.setArguments(args);
        dialog.show(fm, TAG);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mItems = (List) getArguments().getSerializable(ITEMS);
        mTitle = getArguments().getString(TITLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Getting the layout inflater to inflate the view in an alert dialog.
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View rootView = inflater.inflate(R.layout.dialog_searchable_list, null);
        setData(rootView);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setView(rootView);

        String strPositiveButton = _strPositiveButtonText == null ? "CLOSE" : _strPositiveButtonText;
        alertDialog.setPositiveButton(strPositiveButton, _onClickListener);

        String strTitle = mTitle == null ? "Select Item" : mTitle;
        alertDialog.setTitle(strTitle);

        final AlertDialog dialog = alertDialog.create();
        return dialog;
    }

    public void setTitle(String strTitle) {
        mTitle = strTitle;
    }
    public void setOnSearchableItemClickListener(SearchableItem searchableItem) {
        this._searchableItem = searchableItem;
    }

    private void setData(View rootView) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
                .SEARCH_SERVICE);

        _searchView = (SearchView) rootView.findViewById(R.id.search);
        _searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName
                ()));
        _searchView.setIconifiedByDefault(false);
        _searchView.setOnQueryTextListener(this);
        _searchView.setOnCloseListener(this);
        _searchView.clearFocus();

        _listViewItems = (ListView) rootView.findViewById(R.id.listItems);

        //create the adapter by passing your ArrayList data
        listAdapter = new ArrayAdapter(getActivity(), R.layout.item_list_spinner, mItems);
        //attach the adapter to the list
        _listViewItems.setAdapter(listAdapter);
        _listViewItems.setTextFilterEnabled(true);
        _listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _searchableItem.onSearchableItemClicked(listAdapter.getItem(position), position);
                getDialog().dismiss();
            }
        });
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        _searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (TextUtils.isEmpty(s)) {
            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(null);
        } else {
            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(s);
        }
        if (null != _onSearchTextChanged) {
            _onSearchTextChanged.onSearchTextChanged(s);
        }
        return true;
    }
}