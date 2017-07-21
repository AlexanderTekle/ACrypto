package dev.dworks.apps.acrypto.portfolio;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Cache;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import org.fabiomsr.moneytextview.MoneyTextView;

import java.util.Map;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.common.RecyclerFragment.RecyclerItemClickListener.OnItemClickListener;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinPairs;
import dev.dworks.apps.acrypto.entity.Portfolio;
import dev.dworks.apps.acrypto.entity.PortfolioCoin;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getDisplayPercentageSimple;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;
import static dev.dworks.apps.acrypto.utils.Utils.getValueDifferenceColor;

/**
 * Created by HaKr on 08/07/17.
 */

public class PortfolioCoinAdapter extends FirebaseRecyclerAdapter<PortfolioCoin, RecyclerView.ViewHolder> {

    private final Context context;
    final OnItemClickListener onItemClickListener;
    final RecyclerFragment.onDataChangeListener onDataChangeListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mBaseImageUrl;
    private App appInstance;
    private Portfolio mPortfolio;
    private ArrayMap<String, PortfolioCoin> mCoins;
    private String cachedUrl = "";

    public PortfolioCoinAdapter(Context context, Query ref,
                                OnItemClickListener onItemClickListener,
                                RecyclerFragment.onDataChangeListener onDataChangeListener) {
        super(PortfolioCoin.class, R.layout.item_list_price_alert, RecyclerView.ViewHolder.class, ref);
        this.onItemClickListener = onItemClickListener;
        this.onDataChangeListener = onDataChangeListener;
        this.context = context;
        appInstance = App.getInstance();
    }

    @Override
    protected void populateViewHolder(RecyclerView.ViewHolder holder,
                                      PortfolioCoin portfolioCoin, int position) {
        if (isPositionHeader(position)){
            ((PortfolioCoinAdapter.HeaderViewHolder)holder).updateData();
        } else {
            ((PortfolioCoinAdapter.ViewHolder)holder).setData(portfolioCoin, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader (position)) {
            return TYPE_HEADER;
        }
        return TYPE_CELL;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public PortfolioCoin getItem(int position) {
        if(position == 0){
            return null;
        }
        return super.getItem(position - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_HEADER : {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list_portfolio_header, parent, false);
                return new HeaderViewHolder(view) {
                };
            }
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list_portfolio_coin, parent, false);
                return new ViewHolder(view) {
                };
            }
        }
        return null;
    }

    public PortfolioCoinAdapter setBaseImageUrl(String baseImageUrl) {
        this.mBaseImageUrl = baseImageUrl;
        return this;
    }

    @Override
    public void onChildChanged(EventType type, DataSnapshot snapshot, int index, int oldIndex) {
        super.onChildChanged(type, snapshot, index, oldIndex);

        if(type == EventType.REMOVED){
            mCoins.remove(snapshot.getKey());
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (null != onDataChangeListener) {
            onDataChangeListener.onDataChanged();
        }
    }

    @Override
    public void onCancelled(DatabaseError error) {
        super.onCancelled(error);
        cleanup();
        if (null != onDataChangeListener) {
            onDataChangeListener.onCancelled();
        }
    }

    public void updateHeaderData(ArrayMap<String, PortfolioCoin> coins, Portfolio portfolio) {
        this.mCoins = coins;
        mPortfolio = portfolio;
    }

    public void setCachedUrl(String url) {
        cachedUrl = url;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final MoneyTextView cost;
        public final MoneyTextView holdings;
        public final MoneyTextView profit;
        public final TextView profitChange;
        public final TextView lastUpdated;

        public HeaderViewHolder (View v) {
            super (v);
            cost = (MoneyTextView) v.findViewById(R.id.cost);
            cost.setDecimalFormat(getMoneyFormat(true));
            holdings = (MoneyTextView) v.findViewById(R.id.holdings);
            holdings.setDecimalFormat(getMoneyFormat(true));
            profit = (MoneyTextView) v.findViewById(R.id.profit);
            profit.setDecimalFormat(getMoneyFormat(true));
            profitChange = (TextView) v.findViewById(R.id.profit_change);
            lastUpdated = (TextView) v.findViewById(R.id.lastupdated);
        }

        public void updateData(){
            double totalcost = 0;
            double totalholdings = 0;
            if(null != mCoins && !mCoins.isEmpty()){
                for (Map.Entry<String, PortfolioCoin> entry : mCoins.entrySet()){
                    PortfolioCoin coin = entry.getValue();
                    totalcost += coin.getTotalConvertedAmount();
                    totalholdings += coin.getTotalConvertedHoldings();
                }
            }
            double diff = totalholdings - totalcost;
            Utils.setTotalPriceValue(cost, totalcost, getCurrencySymbol(mPortfolio.currency));
            Utils.setTotalPriceValue(holdings, totalholdings, getCurrencySymbol(mPortfolio.currency));
            Utils.setTotalPriceValue(profit, diff, getCurrencySymbol(mPortfolio.currency));
            if(totalcost == 0 && totalholdings == 0){
                profitChange.setText("-");
            } else {
                profitChange.setText(getDisplayPercentageSimple(totalcost, totalholdings));
            }
            profitChange.setTextColor(
                    ContextCompat.getColor(profit.getContext(), getPercentDifferenceColor(diff)));
            updateTimestamp();
        }

        public void updateTimestamp(){
            Cache cache = VolleyPlusHelper.with(context).getRequestQueue().getCache();
            Cache.Entry entry = cache.get(cachedUrl);
            if(null != entry) {
                long lastUpdatedTime = entry.serverDate;
                lastUpdated.setVisibility(0 == lastUpdatedTime ? View.INVISIBLE : View.VISIBLE);
                lastUpdated.setText(TimeUtils.getTimeAgo(lastUpdatedTime));
            } else {
                lastUpdated.setVisibility(View.INVISIBLE);
                lastUpdated.setText("");
            }
        }
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView value;
        public final ImageView icon;
        public final MoneyTextView currentPrice;
        public final MoneyTextView profit;
        public final MoneyTextView price;
        public final TextView currentPriceChange;
        public final TextView profitChange;
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
            value = (TextView) v.findViewById(R.id.value);
            icon = (ImageView) v.findViewById(R.id.icon);
            currentPrice = (MoneyTextView) v.findViewById(R.id.current_price);
            currentPrice.setDecimalFormat(getMoneyFormat(true));
            profit = (MoneyTextView) v.findViewById(R.id.profit);
            profit.setDecimalFormat(getMoneyFormat(true));
            price = (MoneyTextView) v.findViewById(R.id.price);
            price.setDecimalFormat(getMoneyFormat(true));
            currentPriceChange = (TextView) v.findViewById(R.id.current_price_change);
            profitChange = (TextView) v.findViewById(R.id.profit_change);
        }

        public void setData(PortfolioCoin portfolioCoin, int position) {
            mPosition = position;
            name.setText(portfolioCoin.coin);
            String totalValue = String.valueOf(portfolioCoin.amount);
            value.setText(totalValue);
            setIcon(portfolioCoin.coin);
            Utils.setPriceValue(price, portfolioCoin.getUnitPrice(), getCurrencySymbol(portfolioCoin.currency));
            setPrices(portfolioCoin, appInstance.getCachedCoinPair(portfolioCoin.getKey()));
        }

        private void setIcon(String symbol) {
            String url = "";
            try {
                final CoinDetailSample.CoinDetail coinDetail = getCoin(symbol);
                url = getCoinUrl(coinDetail);

            } catch (Exception e) {
            }

            icon.setImageUrl(url, VolleyPlusHelper.with(icon.getContext()).getImageLoader());
        }

        private CoinDetailSample.CoinDetail getCoin(String symbol) {
            return App.getInstance().getCoinDetails().coins.get(symbol);
        }

        private String getCoinUrl(CoinDetailSample.CoinDetail coinDetail) {
            return mBaseImageUrl + coinDetail.id + ".png";
        }

        private void setPrices(PortfolioCoin coin, CoinPairs.CoinPair coinPair) {
            currentPrice.setVisibility(View.INVISIBLE);
            profit.setVisibility(View.INVISIBLE);
            if (coinPair != null) {
                double currentPriceValue = coinPair.getCurrentPrice();
                double openPriceValue = coinPair.get24HPrice();
                double diff = currentPriceValue - openPriceValue;

                Utils.setPriceValue(currentPrice, currentPriceValue, getCurrencySymbol(coin.currency));
                currentPriceChange.setText(Utils.getDisplayPercentage(openPriceValue, currentPriceValue));
                currentPriceChange.setTextColor(ContextCompat.getColor(currentPriceChange.getContext(),
                        getValueDifferenceColor(diff)));

                double profitValue = coin.getTotalProfit();
                Utils.setTotalPriceValue(profit, profitValue, getCurrencySymbol(coin.currency));
                profitChange.setText(coin.getProfitChange());
                profitChange.setTextColor(
                        ContextCompat.getColor(profitChange.getContext(),
                                getPercentDifferenceColor(profitValue)));

                currentPrice.setVisibility(View.VISIBLE);
                profit.setVisibility(View.VISIBLE);
            } else {
                currentPriceChange.setText("");
                profitChange.setText("-");
            }
        }
    }
}