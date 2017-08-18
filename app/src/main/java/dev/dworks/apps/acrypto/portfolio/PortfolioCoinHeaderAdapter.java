package dev.dworks.apps.acrypto.portfolio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fabiomsr.moneytextview.MoneyTextView;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.PortfolioCoinHeader;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.RecycledPagerAdapter;
import dev.dworks.apps.acrypto.view.RecycledPagerAdapter.ViewHolder;

import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;

public class PortfolioCoinHeaderAdapter extends RecycledPagerAdapter<ViewHolder> {

    private PortfolioCoinHeader data;
    private LayoutInflater layoutInflater;

    public PortfolioCoinHeaderAdapter(){
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view;
        switch (viewType) {
            case 0: {
                view = layoutInflater.inflate(R.layout.item_list_portfolio_header_one, parent, false);
                return new ViewHolderOne(view) {
                };
            }
            case 1: {
                view = layoutInflater.inflate(R.layout.item_list_portfolio_header_two, parent, false);
                return new ViewHolderTwo(view) {
                };
            }
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if(null == data){
            return;
        }
        if(position == 0) {
            ((ViewHolderOne)viewHolder).show(data);
        } else if(position == 1){
            ((ViewHolderTwo)viewHolder).show(data);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setData(PortfolioCoinHeader data) {
        if (this.data != data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolderOne extends ViewHolder {
        private final MoneyTextView holdings;
        private final MoneyTextView profit;
        private final TextView profitChange;
        private final MoneyTextView profit24H;
        private final TextView profitChange24H;
        private final TextView currency;

        public ViewHolderOne(View v) {
            super(v);
            holdings = (MoneyTextView) v.findViewById(R.id.holdings);
            holdings.setDecimalFormat(getMoneyFormat(true));
            profit = (MoneyTextView) v.findViewById(R.id.profit);
            profit.setDecimalFormat(getMoneyFormat(true));
            profitChange = (TextView) v.findViewById(R.id.profit_change);
            profit24H = (MoneyTextView) v.findViewById(R.id.profit24H);
            profit24H.setDecimalFormat(getMoneyFormat(true));
            profitChange24H = (TextView) v.findViewById(R.id.profitChange24H);
            currency = (TextView) v.findViewById(R.id.currency);
        }

        public void show(PortfolioCoinHeader header) {
            if(null != currency) {
                currency.setText(header.getCurrency());
            }
            Utils.setTotalPriceValue(holdings, header.getTotalHoldings(), header.getCurrencySymbol());
            Utils.setTotalPriceValue(profit, header.getTotalProfit(), header.getCurrencySymbol());
            profitChange.setText(header.getTotalProfitPercentage());
            profitChange.setTextColor(header.getProfitPercentageColor(profitChange.getContext()));

            Utils.setTotalPriceValue(profit24H, header.getTotalProfit24H(), header.getCurrencySymbol());
            profitChange24H.setText(header.getTotalProfitPercentage24H());
            profitChange24H.setTextColor(header.getProfitPercentage24HColor(profitChange24H.getContext()));
        }
    }

    public static class ViewHolderTwo extends RecycledPagerAdapter.ViewHolder {
        private final MoneyTextView cost;
        private final MoneyTextView realizedProfit;
        private final TextView currency;

        public ViewHolderTwo(View v) {
            super(v);
            cost = (MoneyTextView) v.findViewById(R.id.cost);
            cost.setDecimalFormat(getMoneyFormat(true));
            realizedProfit = (MoneyTextView) v.findViewById(R.id.realizedProft);
            realizedProfit.setDecimalFormat(getMoneyFormat(true));
            currency = (TextView) v.findViewById(R.id.currency);
        }

        public void show(PortfolioCoinHeader header) {
            if(null != currency) {
                currency.setText(header.getCurrency());
            }
            Utils.setTotalPriceValue(realizedProfit, header.getTotalRealizedProfit(), header.getCurrencySymbol());
            Utils.setTotalPriceValue(cost, header.getTotalCost(), header.getCurrencySymbol());
        }
    }
}