package com.bmacode17.androideatitserver.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.interfaces.ItemClickListener;

/**
 * Created by User on 10-Aug-18.
 */

public class OrderDetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textView_productName, textView_productQuantity, textView_productPrice, textView_productDiscount;
    private ItemClickListener itemClickListener;

    public OrderDetailsViewHolder(View itemView) {
        super(itemView);

        textView_productName = (TextView) itemView.findViewById(R.id.textView_productName);
        textView_productQuantity = (TextView) itemView.findViewById(R.id.textView_productQuantity);
        textView_productPrice = (TextView) itemView.findViewById(R.id.textView_productPrice);
        textView_productDiscount = (TextView) itemView.findViewById(R.id.textView_productDiscount);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
