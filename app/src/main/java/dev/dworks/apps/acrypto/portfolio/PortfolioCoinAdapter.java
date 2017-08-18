package dev.dworks.apps.acrypto.portfolio;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import org.fabiomsr.moneytextview.MoneyTextView;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.common.RecyclerFragment.RecyclerItemClickListener.OnItemClickListener;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.CoinPairs;
import dev.dworks.apps.acrypto.entity.Portfolio;
import dev.dworks.apps.acrypto.entity.PortfolioCoin;
import dev.dworks.apps.acrypto.entity.PortfolioCoinHeader;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.utils.Utils.getCurrencySymbol;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;
import static dev.dworks.apps.acrypto.utils.Utils.getPercentDifferenceColor;
import static dev.dworks.apps.acrypto.utils.Utils.getValueDifferenceColor;

/**
 * Created by HaKr on 08/07/17.
 */

public class PortfolioCoinAdapter extends FirebaseRecyclerAdapter<PortfolioCoin, RecyclerView.ViewHolder> {

    private final Context context;
    private final OnItemClickListener onItemClickListener;
    private final RecyclerFragment.onDataChangeListener onDataChangeListener;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CELL = 1;
    private String mBaseImageUrl;
    private App appInstance;
    private Portfolio mPortfolio;
    private ArrayMap<String, PortfolioCoin> mCoins = new ArrayMap<>();
    private String cachedUrl = "";
    private PortfolioCoinHeader header;

    public PortfolioCoinAdapter(Context context, Query ref, Portfolio portfolio,
                                OnItemClickListener onItemClickListener,
                                RecyclerFragment.onDataChangeListener onDataChangeListener) {
        super(PortfolioCoin.class, R.layout.item_list_alert_price, RecyclerView.ViewHolder.class, ref);
        this.onItemClickListener = onItemClickListener;
        this.onDataChangeListener = onDataChangeListener;
        this.context = context;
        mPortfolio = portfolio;
        appInstance = App.getInstance();
    }

    @Override
    protected void populateViewHolder(RecyclerView.ViewHolder holder,
                                      PortfolioCoin portfolioCoin, int position) {
        if (isPositionHeader(position)) {
            ((PortfolioCoinAdapter.HeaderViewHolder) holder).updateData();
        } else {
            ((PortfolioCoinAdapter.ViewHolder) holder).setData(portfolioCoin, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
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
        if (position == 0) {
            return null;
        }
        return super.getItem(position - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_HEADER: {
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

        if(type == EventType.ADDED || type == EventType.CHANGED){
            mCoins.put(snapshot.getKey(), snapshot.getValue(PortfolioCoin.class));
        }
        else if (type == EventType.REMOVED) {
            mCoins.remove(snapshot.getKey());
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

    public void updateHeaderData(String url) {
        cachedUrl = url;
        header = new PortfolioCoinHeader(mPortfolio.currency, mCoins);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final ViewPager viewPager;
        private final TextView lastUpdated;
        private PortfolioCoinHeaderAdapter adapter;

        public HeaderViewHolder(View v) {
            super(v);
            viewPager = (ViewPager) v.findViewById(R.id.viewpager);
            lastUpdated = (TextView) v.findViewById(R.id.lastupdated);
            adapter = new PortfolioCoinHeaderAdapter();
            viewPager.setAdapter(adapter);
        }

        public void updateData() {
            if(null == header){
                return;
            }
            header.calculate();
            adapter.setData(header);
            PortfolioCoinHeader.showLastUpdated(context, lastUpdated, cachedUrl);
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
        public final MoneyTextView currentHolding;
        public final MoneyTextView currentAcquisition;
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
            currentHolding = (MoneyTextView) v.findViewById(R.id.currentHolding);
            if (null != currentHolding) {
                currentHolding.setDecimalFormat(getMoneyFormat(true));
            }
            currentAcquisition = (MoneyTextView) v.findViewById(R.id.currentAcquisition);
            if (null != currentAcquisition) {
                currentAcquisition.setDecimalFormat(getMoneyFormat(true));
            }
            currentPriceChange = (TextView) v.findViewById(R.id.current_price_change);
            profitChange = (TextView) v.findViewById(R.id.profit_change);
        }

        public void setData(PortfolioCoin portfolioCoin, int position) {
            mPosition = position;
            name.setText(portfolioCoin.getCoinName());
            String totalValue = String.valueOf(portfolioCoin.amount);
            value.setText(totalValue);
            setIcon(portfolioCoin.coin);
            Utils.setPriceValue(price, portfolioCoin.getUnitPrice(), getCurrencySymbol(portfolioCoin.currency));
            setPrices(portfolioCoin, appInstance.getCachedCoinPair(portfolioCoin.getKey()));
            if (null != currentHolding) {
                Utils.setPriceValue(currentHolding,
                        portfolioCoin.isSellType() ? portfolioCoin.getTotalAmountSold() : portfolioCoin.getTotalHoldings(),
                        getCurrencySymbol(portfolioCoin.currency));
            }
            if (null != currentAcquisition) {
                Utils.setPriceValue(currentAcquisition, portfolioCoin.getTotalAmount(), getCurrencySymbol(portfolioCoin.currency));
            }

            currentPriceChange.setVisibility(Utils.getVisibility(!portfolioCoin.isSellType()));
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
            if (coin.isSellType()) {
                Utils.setPriceValue(currentPrice, coin.priceSold, getCurrencySymbol(coin.currency));
                currentPriceChange.setText("");
                double profitValue = coin.getTotalProfit();
                Utils.setTotalPriceValue(profit, profitValue, getCurrencySymbol(coin.currency));
                profitChange.setText(coin.getProfitChange());
                profitChange.setTextColor(
                        ContextCompat.getColor(profitChange.getContext(),
                                getPercentDifferenceColor(profitValue)));
                currentPrice.setVisibility(View.VISIBLE);
                profit.setVisibility(View.VISIBLE);
                return;
            }
            if (coinPair != null) {
                double currentPriceValue = coinPair.getCurrentPrice();
                double openPriceValue = coinPair.get24HPrice();
                double diff = currentPriceValue - openPriceValue;

                Utils.setPriceValue(currentPrice, currentPriceValue, getCurrencySymbol(coin.currency));
                currentPriceChange.setText(Utils.getDisplayShortPercentage(openPriceValue, currentPriceValue));
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