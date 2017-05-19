package dev.dworks.apps.acrypto.coins;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.support.v4.util.ArrayMap;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.VolleyTickle;

import org.fabiomsr.moneytextview.MoneyTextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.App;
import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.Coins.Coin;
import dev.dworks.apps.acrypto.entity.Symbols;
import dev.dworks.apps.acrypto.misc.ProviderExecutor;
import dev.dworks.apps.acrypto.misc.UrlConstant;
import dev.dworks.apps.acrypto.misc.UrlManager;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

import static dev.dworks.apps.acrypto.misc.UrlConstant.BASE_URL;
import static dev.dworks.apps.acrypto.utils.Utils.getMoneyFormat;

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
    private ArrayMap<String, Double> mPrices = new ArrayMap<String, Double>();

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
        coinSymbols = App.getInstance().getSymbols();
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
        private final TextView algorithm;
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
            imageView = (ImageView) v.findViewById(R.id.image);
            algorithm = (TextView) v.findViewById(R.id.algorithm);
            price = (MoneyTextView) v.findViewById(R.id.price);
            price.setDecimalFormat(getMoneyFormat(true));
        }

        public void setData(int position){
            mPosition = position;
            Coin item = mData.get(position);

            name.setText(item.coinName);
            String url = BASE_URL + item.imageUrl;
            imageView.setImageUrl(url, VolleyPlusHelper.with(imageView.getContext()).getImageLoader());
            algorithm.setText(item.algorithm);

            Double priceValue = mPrices.containsKey(item.name) ? mPrices.get(item.name) : -1;
            price.setVisibility(View.INVISIBLE);
            if (priceValue != -1) {
                Utils.setPriceValue(price, priceValue, "$");
                price.setVisibility(View.VISIBLE);
            } else {
                final PriceFetchAsyncTask task = new PriceFetchAsyncTask(price, item.name, "USD");
                price.setTag(task);
                ProviderExecutor.forAuthority("CC").execute(task);
            }
        }

        @Override
        public void onClick(View v) {
            if(null != onItemClickListener){
                onItemClickListener.onItemViewClick(v, mPosition);
            }
        }
    }

    public class PriceFetchAsyncTask extends AsyncTask<Uri, Void, Double> implements ProviderExecutor.Preemptable {
        private final MoneyTextView mPriceView;
        private final String mCurrencyFrom;
        private final String mCurrencyTo;
        private final CancellationSignal mSignal;

        public PriceFetchAsyncTask(MoneyTextView sizeView, String currencyFrom, String currencyTo) {
            mPriceView = sizeView;
            mCurrencyFrom = currencyFrom;
            mCurrencyTo = currencyTo;
            mSignal = new CancellationSignal();
        }

        @Override
        public void preempt() {
            cancel(false);
            mSignal.cancel();
        }

        @Override
        protected Double doInBackground(Uri... params) {
            if (isCancelled())
                return null;

            return loadPriceData();
        }

        private double loadPriceData() {
            double currentValue = 0L;
            ArrayMap<String, String> params = new ArrayMap<>();
            params.put("fsym", mCurrencyFrom);
            params.put("tsyms", mCurrencyTo);
            String url = UrlManager.with(UrlConstant.HISTORY_PRICE_URL)
                    .setDefaultParams(params).getUrl();

            StringRequest request = new StringRequest(url, null,null);
            NetworkResponse response = VolleyPlusHelper.with().startTickle(request);
            if (response.statusCode == 200 || response.statusCode == 201) {
                String data = VolleyTickle.parseResponse(response);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    currentValue = jsonObject.getDouble(mCurrencyTo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return currentValue;
        }

        @Override
        protected void onPostExecute(Double result) {
            if (isCancelled()) {
                result = null;
            }
            if (mPriceView.getTag() == this && result != null) {
                mPriceView.setTag(null);
                Utils.setPriceValue(mPriceView, result, "$");
                mPriceView.setVisibility(View.VISIBLE);
                mPrices.put(mCurrencyFrom, result);
            }
        }
    }
}