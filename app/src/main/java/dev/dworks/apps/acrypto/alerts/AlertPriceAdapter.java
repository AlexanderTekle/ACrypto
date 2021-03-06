package dev.dworks.apps.acrypto.alerts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import org.fabiomsr.moneytextview.MoneyTextView;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.common.RecyclerFragment.RecyclerItemClickListener.OnItemClickListener;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.AlertPrice;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.settings.SettingsActivity.CONDITION_DEFAULT;
import static dev.dworks.apps.acrypto.settings.SettingsActivity.FREQUENCY_DEFAULT;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;

/**
 * Created by HaKr on 06/07/17.
 */

public class AlertPriceAdapter extends FirebaseRecyclerAdapter<AlertPrice, AlertPriceAdapter.ViewHolder> {

    private final Context context;
    final OnItemClickListener onItemClickListener;
    final RecyclerFragment.onDataChangeListener onDataChangeListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mBaseImageUrl;

    public AlertPriceAdapter(Context context, Query ref,
                             OnItemClickListener onItemClickListener,
                             RecyclerFragment.onDataChangeListener onDataChangeListener) {
        super(AlertPrice.class, R.layout.item_list_alert_price, AlertPriceAdapter.ViewHolder.class, ref);
        this.onItemClickListener = onItemClickListener;
        this.onDataChangeListener = onDataChangeListener;
        this.context = context;
    }

    @Override
    protected void populateViewHolder(AlertPriceAdapter.ViewHolder holder, AlertPrice alertPrice, int position) {
        holder.setData(alertPrice, position);
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
                        .inflate(R.layout.item_list_alert_price, parent, false);
                return new ViewHolder(view) {
                };
            }
        }
        return null;
    }

    public AlertPriceAdapter setBaseImageUrl(String baseImageUrl) {
        this.mBaseImageUrl = baseImageUrl;
        return this;
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

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private final TextView name;
        private final TextView exchange;
        private final ImageView icon;
        private final ImageView frequency;
        private final MoneyTextView value;
        private final TextView condition;
        private final SwitchCompat status_switch;
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
            exchange = (TextView) v.findViewById(R.id.exchange);
            icon = (ImageView) v.findViewById(R.id.icon);
            frequency = (ImageView) v.findViewById(R.id.frequency);
            value = (MoneyTextView) v.findViewById(R.id.value);
            value.setDecimalFormat(getMoneyFormat(true));
            condition = (TextView) v.findViewById(R.id.condition);
            status_switch = (SwitchCompat) v.findViewById(R.id.status_switch);
        }

        public void setData(AlertPrice alertPrice, int position) {
            mPosition = position;
            String[] nameArray = alertPrice.name.split("-");
            name.setText(nameArray[0] + "/" + nameArray[1]);
            String url = "";
            try {
                final CoinDetailSample.CoinDetail coinDetail = getCoin(nameArray[0]);
                url = getCoinUrl(coinDetail);

            } catch (Exception e) {
            }
            if (nameArray.length == 3) {
                exchange.setText(nameArray[2]);
                exchange.setVisibility(View.VISIBLE);
            } else {
                exchange.setText(null);
                exchange.setVisibility(View.GONE);
            }
            value.setAmount((float) alertPrice.value);
            Utils.setPriceValue(value, alertPrice.value, alertPrice.toSymbol);
            icon.setImageUrl(url, VolleyPlusHelper.with(icon.getContext()).getImageLoader());

            setType(alertPrice.frequency);
            setCondition(alertPrice.condition);
            status_switch.setChecked(alertPrice.status == 1);
            status_switch.setOnCheckedChangeListener(this);
        }

        void setType(String frequency) {
            int resId = frequency.compareTo(FREQUENCY_DEFAULT) == 0
                    ? R.drawable.ic_onetime : R.drawable.ic_persistent;
            this.frequency.setImageResource(resId);
        }

        void setCondition(String value) {
            condition.setText((value.equals(CONDITION_DEFAULT) ? "Less" : "Greater") + " than");
        }

        private CoinDetailSample.CoinDetail getCoin(String symbol) {
            return App.getInstance().getCoinDetails().coins.get(symbol);
        }

        private String getCoinUrl(CoinDetailSample.CoinDetail coinDetail) {
            return mBaseImageUrl + coinDetail.id + ".png";
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (null != onItemClickListener) {
                onItemClickListener.onItemViewClick(compoundButton, mPosition);
            }
        }
    }
}