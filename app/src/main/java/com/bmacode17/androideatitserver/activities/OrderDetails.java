package com.bmacode17.androideatitserver.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.adapters.OrderDetailsAdapter;
import com.bmacode17.androideatitserver.commons.Common;

public class OrderDetails extends AppCompatActivity {

    TextView textView_orderId , textView_orderPhone , textView_orderTotal , textView_orderAddress , textView_orderNotes;
    RecyclerView recyclerView_listFoods;
    RecyclerView.LayoutManager layoutManager;
    String orderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        recyclerView_listFoods = (RecyclerView) findViewById(R.id.recyclerView_listFoods);
        recyclerView_listFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView_listFoods.setLayoutManager(layoutManager);

        textView_orderId = (TextView) findViewById(R.id.textView_orderId);
        textView_orderPhone = (TextView) findViewById(R.id.textView_orderPhone);
        textView_orderTotal = (TextView) findViewById(R.id.textView_orderTotal);
        textView_orderAddress = (TextView) findViewById(R.id.textView_orderAddress);
        textView_orderNotes = (TextView) findViewById(R.id.textView_orderNotes);

        if(getIntent() != null)
            orderId = getIntent().getStringExtra("orderId");

        textView_orderId.setText(orderId);
        textView_orderPhone.setText(Common.currentRequest.getPhone());
        textView_orderTotal.setText(Common.currentRequest.getTotal());
        textView_orderAddress.setText(Common.currentRequest.getAddress());
        textView_orderNotes.setText(Common.currentRequest.getNotes());

        OrderDetailsAdapter adapter = new OrderDetailsAdapter(Common.currentRequest.getFood(),getBaseContext());
        adapter.notifyDataSetChanged();
        recyclerView_listFoods.setAdapter(adapter);
    }
}
