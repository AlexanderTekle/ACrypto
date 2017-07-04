package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.widget.ListAdapter;

import com.jaredrummler.materialspinner.MaterialSpinnerBaseAdapter;

import java.util.ArrayList;
import java.util.List;

final class MaterialSpinnerAdapterWrapper extends MaterialSpinnerBaseAdapter {
    private final ListAdapter listAdapter;

    public MaterialSpinnerAdapterWrapper(Context context, ListAdapter toWrap) {
        super(context);
        this.listAdapter = toWrap;
    }

    public int getCount() {
        return this.listAdapter.getCount() - 1;
    }

    public Object getItem(int position) {
        return position >= this.getSelectedIndex() ? this.listAdapter.getItem(position + 1) : this.listAdapter.getItem(position);
    }

    public Object get(int position) {
        return this.listAdapter.getItem(position);
    }

    public List<Object> getItems() {
        ArrayList items = new ArrayList();

        for (int i = 0; i < this.getCount(); ++i) {
            items.add(this.getItem(i));
        }

        return items;
    }
}
