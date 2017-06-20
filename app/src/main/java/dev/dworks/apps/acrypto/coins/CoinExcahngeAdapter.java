package dev.dworks.apps.acrypto.coins;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.RoundedNumberFormat;
import dev.dworks.apps.acrypto.utils.Utils;

import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageSimple;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinExcahngeAdapter extends RecyclerView.Adapter<CoinExcahngeAdapter.ViewHolder> implements Filterable{

    private final Symbols coinSymbols;
    ArrayList<Coins.CoinDetail> mData;
    ArrayList<Coins.CoinDetail> mOrigData;

    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mBaseImageUrl;
    private String mCurrencySymbol;

    public CoinExcahngeAdapter(RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        mData = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        coinSymbols = App.getInstance().getSymbols();
    }

    public CoinExcahngeAdapter setBaseImageUrl(String baseImageUrl) {
        this.mBaseImageUrl = baseImageUrl;
        return this;
    }

    public CoinExcahngeAdapter setCurrencySymbol(String currencySymbol) {
        this.mCurrencySymbol = currencySymbol;
        return this;
    }

    public void setData(ArrayList<Coins.CoinDetail> contents){
        this.mData = contents;
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
                        .inflate(R.layout.item_list_coin_exchange, parent, false);
                return new ViewHolder(view) {
                };
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(CoinExcahngeAdapter.ViewHolder holder, int position) {
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
                ArrayList<Coins.CoinDetail> filteredList = new ArrayList<>();
                if (null == prefix || prefix.length() == 0) {
                    filteredList.addAll(mOrigData);
                } else {
                    String prefixString = prefix.toString().toLowerCase().trim();
                    final int count = mOrigData.size();

                    for (int i = 0; i < count; i++) {
                        final Coins.CoinDetail value = mOrigData.get(i);
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
                    mData.addAll((ArrayList<Coins.CoinDetail>) results.values);
                } else {
                    mData.clear();
                }
                notifyDataSetChanged();

            }
        };
    }

    public Coins.CoinDetail getItem(int position){
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
        private final TextView change;
        private final MoneyTextView volume;
        private final MoneyTextView price;
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
            change = (TextView) v.findViewById(R.id.change);
            volume = (MoneyTextView) v.findViewById(R.id.volume);
            volume.setDecimalFormat(new RoundedNumberFormat());
            price = (MoneyTextView) v.findViewById(R.id.price);
            price.setDecimalFormat(getMoneyFormat(true));
        }

        public void setData(int position){
            mPosition = position;
            Coins.CoinDetail item = mData.get(position);
            String url = "";
            try {
                name.setText(item.market);

            } catch (Exception e){
                name.setText(item.fromSym);
            }
            Utils.setNumberValue(volume, Double.parseDouble(item.volume24HTo), mCurrencySymbol);
            Utils.setPriceValue(price, Double.parseDouble(item.price), mCurrencySymbol);
            setDifference(item);
        }

        @Override
        public void onClick(View v) {
            if(null != onItemClickListener){
                onItemClickListener.onItemViewClick(v, mPosition);
            }
        }

        private void setDifference(Coins.CoinDetail item){
            double currentPrice = Double.parseDouble(item.price);
            double prevPrice = Double.parseDouble(item.open24H);
            Double difference = (currentPrice - prevPrice);
            change.setText(getDisplayPercentageSimple(prevPrice, currentPrice));
            change.setTextColor(ContextCompat.getColor(change.getContext(), getPercentDifferenceColor(difference)));
        }
    }
}