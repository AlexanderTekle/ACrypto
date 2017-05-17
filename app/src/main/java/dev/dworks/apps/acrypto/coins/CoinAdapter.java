package dev.dworks.apps.acrypto.coins;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.entity.Coins.Coin;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.misc.UrlConstant.BASE_URL;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.ViewHolder> implements Filterable{

    private final Symbols coinSymbols;
    SortedList<Coin> mData;
    ArrayList<Coin> mOrigData;

    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mBaseImageUrl;

    public CoinAdapter(RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        mData = new SortedList<Coin>(Coin.class, new SortedListAdapterCallback<Coin>(this) {
            @Override
            public int compare(Coin o1, Coin o2) {
                if (Integer.valueOf(o1.sortOrder) < Integer.valueOf(o2.sortOrder)) {
                    return -1;
                } else if (Integer.valueOf(o1.sortOrder) > Integer.valueOf(o2.sortOrder)) {
                    return 1;
                }
                return 0;
            }

            @Override
            public boolean areContentsTheSame(Coin oldItem, Coin newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(Coin item1, Coin item2) {
                return false;
            }
        });
        this.onItemClickListener = onItemClickListener;
        coinSymbols = App.getInstance().getCoinSymbols();
    }

    public CoinAdapter setBaseImageUrl(String baseImageUrl) {
        this.mBaseImageUrl = baseImageUrl;
        return this;
    }

    public void setData(ArrayList<Coin> contents){
        this.mData.addAll(contents);
        this.mOrigData = contents;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_CELL;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list_coin, parent, false);
                return new ViewHolder(view) {
                };
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(CoinAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_CELL:
                holder.setData(position);
                break;
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                ArrayList<Coin> filteredList = new ArrayList<>();
                if (null == prefix || prefix.length() == 0) {
                    filteredList.addAll(mOrigData);
                } else {
                    String prefixString = prefix.toString().toLowerCase().trim();
                    final int count = mOrigData.size();

                    for (int i = 0; i < count; i++) {
                        final Coin value = mOrigData.get(i);
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
                    mData.clear();
                    mData.addAll((ArrayList<Coin>) results.values);
                } else {
                    mData.clear();
                }
                notifyDataSetChanged();

            }
        };
    }

    public Coin getItem(int position){
        return mData.get(position);
    }

    public void clear(){
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView name;
        private final ImageView imageView;
        private final TextView cost;
        private final TextView algorithm;
        private final TextView restaurant;
        private int mPosition;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
            name = (TextView) v.findViewById(R.id.name);
            cost = (TextView) v.findViewById(R.id.cost);
            imageView = (ImageView) v.findViewById(R.id.image);
            algorithm = (TextView) v.findViewById(R.id.algorithm);
            restaurant = (TextView) v.findViewById(R.id.proof);
        }

        public void setData(int position){
            mPosition = position;
            Coin item = mData.get(position);

            name.setText(item.coinName);
            String url = BASE_URL + item.imageUrl;
            imageView.setImageUrl(url, VolleyPlusHelper.with().getImageLoader());
            String symbol = item.name;
            if(coinSymbols.coins.containsKey(item.name)){
                symbol = coinSymbols.coins.get(item.name);
            }
            cost.setText(symbol);
            algorithm.setText(item.algorithm);
        }

        @Override
        public void onClick(View v) {
            if(null != onItemClickListener){
                onItemClickListener.onItemViewClick(v, mPosition);
            }
        }
    }
}