package dev.dworks.apps.acrypto.view.searchablespinner;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import dev.dworks.apps.acrypto.R;

public class SearchableSpinner extends AppCompatSpinner implements SearchableItem {

    private Context _context;
    private List _items;
    private SearchableListDialog _searchableListDialog;
    private boolean _isDirty;
    private ArrayAdapter _arrayAdapter;
    private String hint;
    private String title;
    private boolean _isFromInit;

    public SearchableSpinner(Context context) {
        super(context);
        this._context = context;
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner);
        hint = a.getString(R.styleable.SearchableSpinner_hintText);
        title = a.getString(R.styleable.SearchableSpinner_titleText);
        a.recycle();
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
    }

    private void init() {
        _items = new ArrayList();

        _arrayAdapter = (ArrayAdapter) getAdapter();
        if (!TextUtils.isEmpty(hint)) {
            ArrayAdapter arrayAdapter = new ArrayAdapter(_context, android.R.layout.simple_list_item_1, new String[]{hint});
            _isFromInit = true;
            setAdapter(arrayAdapter);
        }
    }

    @Override
    public boolean performClick() {
        if (null != _arrayAdapter) {

            // Refresh content #6
            // Change Start
            // Description: The items were only set initially, not reloading the data in the
            // spinner every time it is loaded with items in the adapter.
            _items.clear();
            for (int i = 0; i < _arrayAdapter.getCount(); i++) {
                _items.add(_arrayAdapter.getItem(i));
            }
            // Change end.

            if(isEnabled()) {
                showListDialog();
            }
        }
        return true;
    }

    private void showListDialog() {
        if (_context == null) {
            return;
        }
        FragmentManager fm = null;
        if (_context instanceof Activity) {
            fm = ((AppCompatActivity) _context).getSupportFragmentManager();
        } else if (_context instanceof ContextWrapper) {
            fm = ((AppCompatActivity) ((ContextWrapper) _context).getBaseContext()).getSupportFragmentManager();
        }
        if(null != fm) {
            _searchableListDialog = SearchableListDialog.show(fm, _items, title);
            _searchableListDialog.setOnSearchableItemClickListener(this);
        }
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {

        if (!_isFromInit) {
            _arrayAdapter = (ArrayAdapter) adapter;
            if (!TextUtils.isEmpty(hint) && !_isDirty) {
                ArrayAdapter arrayAdapter = new ArrayAdapter(_context, android.R.layout.simple_list_item_1, new String[]{hint});
                super.setAdapter(arrayAdapter);
            } else {
                super.setAdapter(adapter);
            }

        } else {
            _isFromInit = false;
            super.setAdapter(adapter);
        }
    }

    @Override
    public void onSearchableItemClicked(Object item, int position) {
        searchableItemClick(item);
    }

    private void searchableItemClick(Object item) {
        int index = _items.indexOf(item);
        setSelection(index);
        searchableItemClick(_items.indexOf(item));
    }

    public void searchableItemClick(int position) {
        if (!_isDirty || position <= _arrayAdapter.getCount() - 1) {
            _isDirty = true;
            setAdapter(_arrayAdapter);
            setSelection(position);
        }
    }

    @Override
    public int getSelectedItemPosition() {
        return (!TextUtils.isEmpty(hint) && !_isDirty) ? AdapterView.INVALID_POSITION : super.getSelectedItemPosition();
    }

    @Override
    public Object getSelectedItem() {
        return (!TextUtils.isEmpty(hint) && !_isDirty) ? null : super.getSelectedItem();
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}