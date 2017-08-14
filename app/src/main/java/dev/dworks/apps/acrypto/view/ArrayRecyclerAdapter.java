package dev.dworks.apps.acrypto.view;

import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.dworks.apps.acrypto.entity.Coins;

public abstract class ArrayRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Filterable{

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private ArrayList<T> mObjects;
    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();
    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    private ArrayList<T> mOrigObjects;

    public ArrayRecyclerAdapter() {
        this(new ArrayList<T>());
    }

    public ArrayRecyclerAdapter(T[] objects) {
        this(new ArrayList<>(Arrays.asList(objects)));
    }

    public ArrayRecyclerAdapter(ArrayList<T> objects) {
        mObjects = objects;
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        int pos;
        synchronized (mLock) {
            pos = getItemCount();
            mObjects.add(object);
        }
        if (mNotifyOnChange) notifyItemInserted(pos);
    }
    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        int pos;
        synchronized (mLock) {
            pos = getItemCount();
            mObjects.addAll(collection);
        }
        if (mNotifyOnChange) notifyItemRangeInserted(pos, collection.size());
    }
    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T ... items) {
        int start;
        synchronized (mLock) {
            start = getItemCount();
            Collections.addAll(mObjects, items);
        }
        if (mNotifyOnChange) notifyItemRangeInserted(start, items.length);
    }
    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            mObjects.add(index, object);
        }
        if (mNotifyOnChange) notifyItemInserted(index);
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void setData(Collection<? extends T>  collection){
        synchronized (mLock) {
            mObjects = new ArrayList<T>(collection);
            mOrigObjects = new ArrayList<>(mObjects);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        int pos;
        synchronized (mLock) {
            pos = getPosition(object);
            if(pos == -1) return;
            mObjects.remove(pos);
        }
        if (mNotifyOnChange) notifyItemRemoved(pos);
    }
    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }
    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            Collections.sort(mObjects, comparator);
            mOrigObjects = new ArrayList<>(mObjects);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    /**
     * {@inheritDoc}
     */
    public int getItemCount() {
        return mObjects.size();
    }
    /**
     * {@inheritDoc}
     */
    public T getItem(int position) {
        return mObjects.get(position);
    }
    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }
    /**
     * {@inheritDoc}
     */
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                ArrayList<T> filteredList = new ArrayList<>();
                if (null == prefix || prefix.length() == 0) {
                    filteredList.addAll(mOrigObjects);
                } else {
                    String prefixString = prefix.toString().toLowerCase().trim();
                    final int count = mOrigObjects.size();

                    for (int i = 0; i < count; i++) {
                        final T value = mOrigObjects.get(i);
                        final String valueText = value.toString().toLowerCase();

                        // First match against the whole, non-splitted value
                        if (valueText.contains(prefixString)) {
                            filteredList.add(value);
                        } else {
                            final String[] words = prefixString.split("\\s+");
                            for (String word : words){
                                if (valueText.contains(word)) {
                                    filteredList.add(value);
                                    break;
                                }
                            }
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mObjects.clear();
                    mObjects.addAll((Collection<? extends T>) results.values);
                } else {
                    mObjects.clear();
                }
                notifyDataSetChanged();
            }
        };
    }

    public void filter(String query){
        synchronized (mLock) {
            getFilter().filter(query);
        }
    }
}