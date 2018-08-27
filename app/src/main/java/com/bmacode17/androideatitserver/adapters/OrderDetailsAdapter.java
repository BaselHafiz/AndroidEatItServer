package com.bmacode17.androideatitserver.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.models.Order;
import com.bmacode17.androideatitserver.viewHolders.OrderDetailsViewHolder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by User on 10-Aug-18.
 */

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsViewHolder> {

    private List<Order> listData = new ArrayList<>();
    private Context context;

    public OrderDetailsAdapter(List<Order> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @Override
    public OrderDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.cardview_order_details,parent,false);
        return new OrderDetailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderDetailsViewHolder holder, int position) {

        Order order = listData.get(position);
        holder.textView_productName.setText(String.format("Name : %s",order.getProductName()));
        holder.textView_productQuantity.setText(String.format("Quantity : %s",order.getQuantity()));
        holder.textView_productPrice.setText(String.format("Price : %s",order.getPrice()));
        holder.textView_productDiscount.setText(String.format("Discount : %s",order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
