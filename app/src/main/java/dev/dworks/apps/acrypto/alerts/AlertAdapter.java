package dev.dworks.apps.acrypto.alerts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import org.fabiomsr.moneytextview.MoneyTextView;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.CoinDetailSample;
import dev.dworks.apps.acrypto.entity.PriceAlert;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;

/**
 * Created by HaKr on 06/07/17.
 */

public class AlertAdapter extends FirebaseRecyclerAdapter<PriceAlert, AlertAdapter.ViewHolder>{

    private final Context context;
    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    private String mBaseImageUrl;

    public AlertAdapter(Context context, Query ref, RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        super(PriceAlert.class, R.layout.item_list_price_alert, AlertAdapter.ViewHolder.class, ref);
        this.onItemClickListener = onItemClickListener;
        this.context = context;
    }

    @Override
    protected void populateViewHolder(AlertAdapter.ViewHolder holder, PriceAlert priceAlert, int position) {
        holder.setData(priceAlert, position);
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
                        .inflate(R.layout.item_list_price_alert, parent, false);
                return new ViewHolder(view) {
                };
            }
        }
        return null;
    }

    public AlertAdapter setBaseImageUrl(String baseImageUrl) {
        this.mBaseImageUrl = baseImageUrl;
        return this;
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private final TextView name;
        private final TextView exchange;
        private final ImageView icon;
        private final MoneyTextView value;
        private final TextView status;
        private final Switch status_switch;
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
            value = (MoneyTextView) v.findViewById(R.id.value);
            value.setDecimalFormat(getMoneyFormat(true));
            status = (TextView) v.findViewById(R.id.status);
            status_switch = (Switch) v.findViewById(R.id.status_switch);
        }

        public void setData(PriceAlert priceAlert, int position){
            mPosition = position;
            String[] nameArray = priceAlert.name.split("-");
            name.setText(nameArray[0]+"/"+nameArray[1]);
            String url = "";
            try {
                final CoinDetailSample.CoinDetail coinDetail = getCoin(nameArray[0]);
                url = getCoinUrl(coinDetail);

            } catch (Exception e){
            }
            if(nameArray.length == 3) {
                exchange.setText(nameArray[2]);
                exchange.setVisibility(View.VISIBLE);
            } else {
                exchange.setText(null);
                exchange.setVisibility(View.GONE);
            }
            value.setAmount((float) priceAlert.value);
            Utils.setPriceValue(value, priceAlert.value, priceAlert.toSymbol);
            icon.setImageUrl(url, VolleyPlusHelper.with(icon.getContext()).getImageLoader());

            setStatus(priceAlert.status == 1);
            status_switch.setChecked(priceAlert.status == 1);
            status_switch.setOnCheckedChangeListener(this);
        }

        void setStatus(boolean enabled){
            status.setText(enabled ? "Enabled" : "Disabled");
        }

        private CoinDetailSample.CoinDetail getCoin(String symbol){
            return App.getInstance().getCoinDetails().coins.get(symbol);
        }

        private String getCoinUrl(CoinDetailSample.CoinDetail coinDetail){
            return mBaseImageUrl + coinDetail.id + ".png";
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            setStatus(b);
            if(null != onItemClickListener){
                onItemClickListener.onItemViewClick(compoundButton, mPosition);
            }
        }
    }
}