package com.bmacode17.androideatitserver.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;

public class BannerViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    public TextView textView_bannerName;
    public ImageView imageView_bannerImage;

    public BannerViewHolder(View itemView) {
        super(itemView);

        textView_bannerName = (TextView) itemView.findViewById(R.id.textView_bannerName);
        imageView_bannerImage = (ImageView) itemView.findViewById(R.id.imageView_bannerImage);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Select the action");

        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
