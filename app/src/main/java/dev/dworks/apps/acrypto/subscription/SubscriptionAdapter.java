package dev.dworks.apps.acrypto.subscription;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import dev.dworks.apps.acrypto.R;
import dev.dworks.apps.acrypto.entity.Subscriptions;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {
    private ArrayList<Subscriptions.Subscription> mDataset;

    public SubscriptionAdapter(ArrayList<Subscriptions.Subscription> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public SubscriptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_subscription, parent, false);
        return new SubscriptionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTitle;
        public final TextView mDescription;
        public final ImageView mIcon;

        public ViewHolder(View v) {
            super(v);
            mTitle = (TextView) v.findViewById(R.id.title);
            mDescription = (TextView) v.findViewById(R.id.description);
            mIcon = (ImageView) v.findViewById(R.id.icon);
        }

        public void setData(int position) {
            Subscriptions.Subscription subscription = mDataset.get(position);
            mTitle.setText(subscription.title);
            mDescription.setText(subscription.description);
            setIcon(subscription.type);
        }

        private void setIcon(int type) {
            int resId = type == 1 ? R.drawable.ic_feature : R.drawable.ic_bonus;
            mIcon.setImageResource(resId);
        }
    }
}
