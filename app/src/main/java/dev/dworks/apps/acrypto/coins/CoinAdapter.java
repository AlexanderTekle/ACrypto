package dev.dworks.apps.acrypto.coins;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.RoundedNumberFormat;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ArrayRecyclerAdapter;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageSimple;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinAdapter extends ArrayRecyclerAdapter<Coins.CoinDetail, CoinAdapter.ViewHolder> {

    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    public static final int SORT_DEFAULT = 0;
    public static final int SORT_PRICE = 1;
    public static final int SORT_VOLUME_CHANGE = 2;
    public static final int SORT_PRICE_CHANGE = 3;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mCurrencySymbol;
    private ArrayList<Coins.CoinDetail> mDefaultData;

    public CoinAdapter(RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public CoinAdapter setCurrencySymbol(String currencySymbol) {
        this.mCurrencySymbol = currencySymbol;
        return this;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_CELL;
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
    public void setData(Collection<? extends Coins.CoinDetail> collection) {
        super.setData(collection);
        mDefaultData = new ArrayList<>(collection);
    }

    public void sortList(int sortType){
        if(sortType == SORT_DEFAULT){
            setData(mDefaultData);
            return;
        }
        sort(new CoinComparator(sortType));
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView name;
        private final TextView change;
        private final ImageView imageView;
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
            imageView = (ImageView) v.findViewById(R.id.image);
            volume = (MoneyTextView) v.findViewById(R.id.volume);
            volume.setDecimalFormat(new RoundedNumberFormat());
            price = (MoneyTextView) v.findViewById(R.id.price);
            price.setDecimalFormat(getMoneyFormat(true));
        }

        public void setData(int position){
            mPosition = position;
            Coins.CoinDetail item = getItem(position);
            String url = "";
            name.setText(item.name + " ("+item.getFromSym()+")");
            url = getCoinUrl(item.id);
            imageView.setImageUrl(url, VolleyPlusHelper.with(imageView.getContext()).getImageLoader());

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

        private CoinDetailSample.CoinDetail getCoin(String symbol){
            return App.getInstance().getCoinDetails().coins.get(symbol);
        }

        private String getCoinUrl(String id){
            return TextUtils.isEmpty(id) ? "" :Coins.BASE_URL + id + ".png";
        }

        private void setDifference(Coins.CoinDetail item){
            double currentPrice = Double.parseDouble(item.price);
            double prevPrice = Double.parseDouble(item.open24H);
            Double difference = (currentPrice - prevPrice);
            change.setText(getDisplayPercentageSimple(prevPrice, currentPrice));
            change.setTextColor(ContextCompat.getColor(change.getContext(), getPercentDifferenceColor(difference)));
        }
    }

    public class CoinComparator implements Comparator<Coins.CoinDetail> {

        private final int sortType;

        public CoinComparator(int sortType){
            this.sortType = sortType;
        }

        @Override
        public int compare(Coins.CoinDetail t0, Coins.CoinDetail t1) {
            switch (sortType){
                case SORT_PRICE:
                    return Double.valueOf(t1.price).compareTo(Double.valueOf(t0.price));
                case SORT_VOLUME_CHANGE:
                    return Double.valueOf(t1.volume24HTo).compareTo(Double.valueOf(t0.volume24HTo));
                case SORT_PRICE_CHANGE:
                    return t1.differnce().compareTo(t0.differnce());
            }
            return Double.valueOf(t1.fromSym).compareTo(Double.valueOf(t0.fromSym));
        }
    }
}