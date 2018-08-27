package com.bmacode17.androideatitserver.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.interfaces.ItemClickListener;

/**
 * Created by User on 22-Jul-18.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textView_orderId, textView_orderStatus, textView_orderPhone, textView_orderAddress;
    public Button button_edit , button_delete , button_details , button_directions;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);

        textView_orderId = (TextView) itemView.findViewById(R.id.textView_orderId);
        textView_orderStatus = (TextView) itemView.findViewById(R.id.textView_orderStatus);
        textView_orderPhone = (TextView) itemView.findViewById(R.id.textView_orderPhone);
        textView_orderAddress = (TextView) itemView.findViewById(R.id.textView_orderAddress);

        button_edit = (Button) itemView.findViewById(R.id.button_edit);
        button_delete = (Button) itemView.findViewById(R.id.button_delete);
        button_details = (Button) itemView.findViewById(R.id.button_details);
        button_directions = (Button) itemView.findViewById(R.id.button_directions);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}

