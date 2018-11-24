package com.bmacode17.androideatitserver.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.interfaces.ItemClickListener;
import com.bmacode17.androideatitserver.models.Food;
import com.bmacode17.androideatitserver.models.MyNotification;
import com.bmacode17.androideatitserver.models.MyResponse;
import com.bmacode17.androideatitserver.models.Order;
import com.bmacode17.androideatitserver.models.Request;
import com.bmacode17.androideatitserver.models.Sender;
import com.bmacode17.androideatitserver.models.Token;
import com.bmacode17.androideatitserver.remote.APIService;
import com.bmacode17.androideatitserver.viewHolders.FoodViewHolder;
import com.bmacode17.androideatitserver.viewHolders.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    private static final String TAG = "Basel";
    FirebaseDatabase database;
    DatabaseReference table_request;
    RecyclerView recyclerView_listOrder;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    MaterialSpinner spinner_status;
    AlertDialog updateOrderDialog;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        // Init service
        mService = Common.getFCMService();

        database = FirebaseDatabase.getInstance();
        table_request = database.getReference("request");

        recyclerView_listOrder = (RecyclerView) findViewById(R.id.recyclerView_listOrder);
        recyclerView_listOrder.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView_listOrder.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {

        // Create query by phone
        Query query = table_request.orderByChild("phone");

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query,Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {

                viewHolder.textView_orderId.setText(adapter.getRef(position).getKey());
                viewHolder.textView_orderStatus.setText(Common.convertStatusToCode(model.getStatus()));
                viewHolder.textView_orderAddress.setText(model.getAddress());
                viewHolder.textView_orderPhone.setText(model.getPhone());

                viewHolder.button_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openUpdateOrderDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

                viewHolder.button_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.button_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent OrderDetailsIntent = new Intent(OrderStatus.this,OrderDetails.class);
                        Common.currentRequest = model;
                        OrderDetailsIntent.putExtra("orderId" , adapter.getRef(position).getKey());
                        startActivity(OrderDetailsIntent);
                    }
                });

                viewHolder.button_directions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrderIntent = new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrderIntent);
                    }
                });

                final Request clickedItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(OrderStatus.this, clickedItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_order,parent,false);
                return new OrderViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_listOrder.setAdapter(adapter);
    }

    private void openUpdateOrderDialog(final String key, final Request item) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_update_order, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Update Order");
        myAlertDialog.setMessage("Please choose status");
        myAlertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        spinner_status = (MaterialSpinner) dialogView.findViewById(R.id.spinner_status);
        spinner_status.setItems("Placed","On the way","Shipped");

        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myAlertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // update information
                item.setStatus(String.valueOf(spinner_status.getSelectedIndex()));
                table_request.child(key).setValue(item);
                adapter.notifyDataSetChanged();
                sendOrderStatusChangeNotificationToUser(key,item);
            }
        });

        updateOrderDialog = myAlertDialog.create();
        updateOrderDialog.show();
    }

    private void sendOrderStatusChangeNotificationToUser(final String key , Request item) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference table_token = db.getReference("token");
        Query data = table_token.orderByKey().equalTo(item.getPhone());
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot item : dataSnapshot.getChildren()) {

                    Token token = item.getValue(Token.class);

                    // Create Raw payload to send
                    MyNotification notification = new MyNotification("Basel", "Your order: " + key + " was updated");
                    Sender content = new Sender(token.getToken(), notification);
                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.body().success == 1) {
                                        Toast.makeText(OrderStatus.this, "Thank you , order is updated ", Toast.LENGTH_LONG).show();
                                    }else
                                        Toast.makeText(OrderStatus.this, "Order is updated but failed to send notification", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d(TAG, "onFailure: Error: " + t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteOrder(String key) {

        table_request.child(key).removeValue();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Order is deleted ! ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(adapter != null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadOrders();
    }
}