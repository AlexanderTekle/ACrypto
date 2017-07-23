package dev.dworks.apps.acrypto.news;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.common.RecyclerFragment;
import dev.dworks.apps.acrypto.entity.News;
import dev.dworks.apps.acrypto.misc.linkpreview.CacheUtils;
import dev.dworks.apps.acrypto.network.VolleyPlusHelper;
import dev.dworks.apps.acrypto.utils.Utils;
import dev.dworks.apps.acrypto.view.ImageView;

/**
 * Created by HaKr on 21/07/17.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> implements Filterable {

    ArrayList<News.NewsData> mData;
    ArrayList<News.NewsData> mOrigData;

    final RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener;
    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    CacheUtils mCache;

    public NewsAdapter(Context context,
                       RecyclerFragment.RecyclerItemClickListener.OnItemClickListener onItemClickListener) {
        mData = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        this.mCache = new CacheUtils(context);
    }

    public void setData(ArrayList<News.NewsData> contents) {
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
                        .inflate(R.layout.item_list_news, parent, false);
                return new ViewHolder(view) {
                };
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(NewsAdapter.ViewHolder holder, int position) {
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
                ArrayList<News.NewsData> filteredList = new ArrayList<>();
                if (null == prefix || prefix.length() == 0) {
                    filteredList.addAll(mOrigData);
                } else {
                    String prefixString = prefix.toString().toLowerCase().trim();
                    final int count = mOrigData.size();

                    for (int i = 0; i < count; i++) {
                        final News.NewsData value = mOrigData.get(i);
                        final String valueText = value.toString().toLowerCase();

                        // First match against the whole, non-splitted value
                        if (valueText.contains(prefixString)) {
                            filteredList.add(value);
                        } else {
                            final String[] words = prefixString.split("\\s+");
                            for (String word : words) {
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
                    mData.addAll((ArrayList<News.NewsData>) results.values);
                } else {
                    mData.clear();
                }
                notifyDataSetChanged();

            }
        };
    }

    public News.NewsData getItem(int position) {
        return mData.get(position);
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
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
            News.NewsData item = mData.get(position);
            String url = "";
            title.setText(item.title);
            //time.setText(TimeUtils.formatHumanFriendlyShortDate(item.publicated * 1000));
            time.setText(item.publish_time);
            channel.setText(Utils.getDomainName(item.link));
            imageView.setImageUrl(item.thumb,
                    VolleyPlusHelper.with(imageView.getContext()).getNewsImageLoader());
        }
    }
}