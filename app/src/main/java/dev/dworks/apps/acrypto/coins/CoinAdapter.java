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
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.Coins;
import dev.dworks.apps.acrypto.misc.RoundedNumberFormat;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageSimple;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;

/**
 * Created by HaKr on 16/05/17.
 */

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.ViewHolder> {

    ArrayList<Coins.CoinDetail> mData;

    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mBaseImageUrl;
    private String mCurrencySymbol;

    public CoinAdapter(RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        mData = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    public CoinAdapter setBaseImageUrl(String baseImageUrl) {
        this.mBaseImageUrl = baseImageUrl;
        return this;
    }

    public CoinAdapter setCurrencySymbol(String currencySymbol) {
        this.mCurrencySymbol = currencySymbol;
        return this;
    }

    public void setData(ArrayList<Coins.CoinDetail> contents){
        this.mData = contents;
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
            Coins.CoinDetail item = mData.get(position);
            String url = "";
            try {
                final CoinDetailSample.CoinDetail coinDetail = getCoin(item.fromSym);
                name.setText(coinDetail.name);
                url = getCoinUrl(coinDetail);

            } catch (Exception e){
                name.setText(item.fromSym);
            }
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

        private String getCoinUrl(CoinDetailSample.CoinDetail coinDetail){
            return mBaseImageUrl + coinDetail.id + ".png";
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