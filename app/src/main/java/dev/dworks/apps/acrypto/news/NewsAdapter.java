package dev.dworks.apps.acrypto.news;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.NativeExpressAdView;

import java.util.ArrayList;
import java.util.List;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.News;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.TimeUtils;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

/**
 * Created by HaKr on 21/07/17.
 */

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int AD_POSITION = 10;
    private final float scale;
    List<Object> mData;

    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    static final int TYPE_AD = 2;
    private boolean mShowAds;

    public NewsAdapter(Context context,
                       RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        mData = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        scale = context.getResources().getDisplayMetrics().density;
    }

    public void setData(List<Object> contents, boolean showAds) {
        this.mData = contents;
        this.mShowAds = showAds;
    }

    @Override
    public int getItemViewType(int position) {
        return mShowAds ?
                ( position % AD_POSITION == 0 && position != 0) ? TYPE_AD : TYPE_CELL
                : TYPE_CELL;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list_news, parent, false);
                return new ViewHolder(view);
            }

            case TYPE_AD : {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list_news_ad, parent, false);
                return new AdViewHolder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_CELL:
                ((ViewHolder)holder).setData(position);
                break;
            case TYPE_AD:
                ((AdViewHolder)holder).setData(position);
                break;
        }
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        private final CardView adCardView;

        public AdViewHolder(View itemView) {
            super(itemView);
            adCardView = (CardView) itemView.findViewById(R.id.cardView);
        }

        public void setData(int position) {
            NativeExpressAdView adView =
                    (NativeExpressAdView) mData.get(position);
            if (adCardView.getChildCount() > 0) {
                adCardView.removeAllViews();
            }
            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            adCardView.addView(adView);
        }
    }
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView channel;
        private final TextView time;
        private final ImageView imageView;
        private int mPosition;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
            title = (TextView) v.findViewById(R.id.title);
            channel = (TextView) v.findViewById(R.id.channel);
            time = (TextView) v.findViewById(R.id.time);
            imageView = (ImageView) v.findViewById(R.id.image);
        }

        public void setData(int position) {
            mPosition = position;
            News.NewsData item = (News.NewsData) mData.get(position);
            String url = "";
            title.setText(item.title);
            time.setText(TimeUtils.getNewsTimestamp(item.publish_time));
            channel.setText(Utils.getDomainName(item.link));
            imageView.setImageUrl(item.thumb,
                    VolleyPlusHelper.with(imageView.getContext()).getNewsImageLoader());
        }
    }
}